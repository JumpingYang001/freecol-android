/**
 *  Copyright (C) 2002-2012   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>ProductionCache</code> is contains all relevant
 * information about the production and consumption of the
 * colony. This includes the production of all colony tiles and
 * buildings, as well as the consumption of all units, buildings and
 * build queues.
 *
 */
public class ProductionCache {

    /**
     * The colony whose production is being cached. The goods stored
     * in the colony may need to be considered in order to prevent
     * excess production.
     */
    private Colony colony;

    private TypeCountMap<GoodsType> netProduction =
        new TypeCountMap<GoodsType>();

    private Map<Object, ProductionInfo> productionAndConsumption =
        new HashMap<Object, ProductionInfo>();

    private Set<GoodsType> goodsUsed = new HashSet<GoodsType>();

    private boolean upToDate = false;


    /**
     * Creates a new <code>ProductionCache</code> instance.
     *
     * @param colony a <code>Colony</code> value
     */
    public ProductionCache(Colony colony) {
        this.colony = colony;
    }


    /**
     * Updates all data structures. The method has no side effects.
     *
     */
    private synchronized void update() {
        if (upToDate) return; // nothing to do
        productionAndConsumption.clear();
        netProduction.clear();
        goodsUsed.clear();
        ProductionMap production = new ProductionMap();
        for (ColonyTile colonyTile : colony.getColonyTiles()) {
            List<AbstractGoods> p = colonyTile.getProduction();
            if (!p.isEmpty()) {
                production.add(p);
                ProductionInfo info = new ProductionInfo();
                info.addProduction(p);
                productionAndConsumption.put(colonyTile, info);
                for (AbstractGoods goods : p) {
                    goodsUsed.add(goods.getType());
                    netProduction.incrementCount(goods.getType().getStoredAs(), goods.getAmount());
                }
            }
        }

        GoodsType bells = colony.getSpecification().getGoodsType("model.goods.bells");
        int unitsThatUseNoBells = colony.getSpecification().getInteger("model.option.unitsThatUseNoBells");
        int amount = Math.min(unitsThatUseNoBells, colony.getUnitCount());
        ProductionInfo bellsInfo = new ProductionInfo();
        bellsInfo.addProduction(new AbstractGoods(bells, amount));
        productionAndConsumption.put(this, bellsInfo);
        netProduction.incrementCount(bells, amount);

        for (Consumer consumer : colony.getConsumers()) {
            Set<Modifier> modifier = consumer.getModifierSet("model.modifier.consumeOnlySurplusProduction");
            List<AbstractGoods> goods = new ArrayList<AbstractGoods>();
            for (AbstractGoods g : consumer.getConsumedGoods()) {
                goodsUsed.add(g.getType());
                AbstractGoods surplus = new AbstractGoods(production.get(g.getType()));
                if (modifier.isEmpty()) {
                    surplus.setAmount(surplus.getAmount() + getGoodsCount(g.getType()));
                } else {
                    surplus.setAmount((int) FeatureContainer.applyModifierSet(surplus.getAmount(),
                                                                              null, modifier));
                }
                goods.add(surplus);
            }
            ProductionInfo info = null;
            if (consumer instanceof Building) {
                Building building = (Building) consumer;
                AbstractGoods output = null;
                GoodsType outputType = building.getGoodsOutputType();
                if (outputType != null) {
                    goodsUsed.add(outputType);
                    output = new AbstractGoods(production.get(outputType));
                    output.setAmount(output.getAmount() + getGoodsCount(outputType));
                }
                info = building.getProductionInfo(output, goods);
            } else if (consumer instanceof Unit) {
                info = ((Unit) consumer).getProductionInfo(goods);
            } else if (consumer instanceof BuildQueue) {
                info = ((BuildQueue<?>) consumer).getProductionInfo(goods);
            }
            if (info != null) {
                production.add(info.getProduction());
                production.remove(info.getConsumption());
                for (AbstractGoods g : info.getProduction()) {
                    netProduction.incrementCount(g.getType().getStoredAs(), g.getAmount());
                }
                for (AbstractGoods g : info.getConsumption()) {
                    netProduction.incrementCount(g.getType().getStoredAs(), -g.getAmount());
                }
                productionAndConsumption.put(consumer, info);
            }
        }
        this.productionAndConsumption = productionAndConsumption;
        this.netProduction = netProduction;
        upToDate = true;
    }


    /**
     * Returns the number of goods of the given type stored in the
     * colony.
     *
     * @param type a <code>GoodsType</code> value
     * @return an <code>int</code> value
     */
    private int getGoodsCount(GoodsType type) {
        return colony.getGoodsCount(type);
    }


    /**
     * Invalidates the production cache. This method needs to be
     * called whenever global production modifiers change. This might
     * be the case when a new {@link FoundingFather} is added, or when
     * the colony's production bonus changes.
     *
     */
    public synchronized void invalidate() {
        upToDate = false;
    }


    /**
     * Invalidates the production cache if it produces or consumes the
     * given GoodsType. This method needs to be called whenever goods
     * are added to or removed from the colony.
     *
     * @param goodsType a <code>GoodsType</code> value
     */
    public synchronized void invalidate(GoodsType goodsType) {
        if (goodsUsed.contains(goodsType)) {
            upToDate = false;
        }
    }


    /**
     * Returns the net production, that is the total production minus
     * the total consumption, of the given GoodsType.
     *
     * @param type a <code>GoodsType</code> value
     * @return an <code>int</code> value
     */
    public int getNetProductionOf(GoodsType type) {
        update();
        return netProduction.getCount(type);
    }

    /**
     * Returns the <code>ProductionInfo</code> for the given {@link
     * WorkLocation} or {@link Consumer}.
     *
     * @param object an <code>Object</code> value
     * @return a <code>ProductionInfo</code> value
     */
    public ProductionInfo getProductionInfo(Object object) {
        update();
        return productionAndConsumption.get(object);
    }


    /**
     * Gets a copy of the current production state.
     *
     * @return A copy of the current production state.
     */
    public TypeCountMap<GoodsType> getProductionMap() {
        update();
        TypeCountMap<GoodsType> result = new TypeCountMap<GoodsType>();
        result.putAll(netProduction);
        return result;
    }
}
