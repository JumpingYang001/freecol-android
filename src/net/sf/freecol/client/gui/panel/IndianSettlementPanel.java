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

package net.sf.freecol.client.gui.panel;

import java.util.logging.Logger;

import org.freecolandroid.repackaged.javax.swing.JLabel;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;

/**
 * This panel is used to show information about an Indian settlement.
 */
public final class IndianSettlementPanel extends FreeColPanel {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(IndianSettlementPanel.class.getName());

    /**
     * The constructor that will add the items to this panel.
     * @param freeColClient 
     *
     * @param canvas <code>Canvas</code>
     * @param settlement <code>IndianSettlement</code>
     */
    public IndianSettlementPanel(FreeColClient freeColClient, GUI gui, IndianSettlement settlement) {
        
        super(freeColClient, gui);

        setLayout(new MigLayout("wrap 2, gapx 20", "", ""));
        
        JLabel settlementLabel = new JLabel(gui.getImageIcon(settlement, false));
        Player indian = settlement.getOwner();
        Player player = getMyPlayer();
        boolean visited = player.hasVisited(settlement);
        String text = Messages.message(settlement.getNameFor(player)) + ", "
            + Messages.message(StringTemplate.template(settlement.isCapital()
                                                       ? "indianCapital"
                                                       : "indianSettlement")
                               .addStringTemplate("%nation%", indian.getNationName()));
        String messageId = settlement.getShortAlarmLevelMessageId(player);
        text += " (" + Messages.message(messageId) + ")";
        settlementLabel.setText(text);
        add(settlementLabel);

        Unit missionary = settlement.getMissionary();
        if (missionary != null) {
            String missionaryName = Messages.message(StringTemplate.template("model.unit.nationUnit")
                                                     .addStringTemplate("%nation%", missionary.getOwner().getNationName())
                                                     .addStringTemplate("%unit%", missionary.getLabel()));
            add(new JLabel(missionaryName, gui.getImageIcon(missionary, true), JLabel.CENTER));
        }

        add(localizedLabel("indianSettlement.learnableSkill"), "newline");
        UnitType skillType = settlement.getLearnableSkill();
        if (visited) {
            if (skillType == null) {
                add(localizedLabel("indianSettlement.skillNone"));
            } else {
                add(new JLabel(Messages.message(skillType.getNameKey()),
                        gui.getImageIcon(skillType, true), JLabel.CENTER));
            }
        } else {
            add(localizedLabel("indianSettlement.skillUnknown"));
        }

        GoodsType[] wantedGoods = settlement.getWantedGoods();
        int n = 0;
        for (int i = 0; i < wantedGoods.length; i++) {
            if (wantedGoods[i] != null) n++;
        }
        String sale;
        add(localizedLabel("indianSettlement.highlyWanted"), "newline");
        if (!visited) {
            add(localizedLabel("indianSettlement.wantedGoodsUnknown"));
        } else if (wantedGoods[0] == null) {
            add(localizedLabel("indianSettlement.wantedGoodsNone"));
        } else {
            sale = player.getLastSaleString(settlement, wantedGoods[0]);
            add(new JLabel(Messages.message(wantedGoods[0].getNameKey())
                           + ((sale == null) ? "" : " " + sale),
                           gui.getImageIcon(wantedGoods[0], false),
                           JLabel.CENTER));
            n--;
        }

        add(localizedLabel("indianSettlement.otherWanted"), "newline");
        if (!visited) {
            add(localizedLabel("indianSettlement.wantedGoodsUnknown"));
        } else if (n == 0) {
            add(localizedLabel("indianSettlement.wantedGoodsNone"));
        } else {
            sale = player.getLastSaleString(settlement, wantedGoods[1]);
            add(new JLabel(Messages.message(wantedGoods[1].getNameKey())
                           + ((sale == null) ? "" : " " + sale),
                           gui.getImageIcon(wantedGoods[1], false),
                           JLabel.CENTER),
                "split " + Integer.toString(n));
            for (int i = 2; i < wantedGoods.length; i++) {
                if (wantedGoods[i] != null) {
                    sale = player.getLastSaleString(settlement,wantedGoods[i]);
                    add(new JLabel(Messages.message(wantedGoods[i].getNameKey())
                                   + ((sale == null) ? "" : " " + sale),
                                   gui.getImageIcon(wantedGoods[i], false),
                                   JLabel.CENTER));
                }
            }
        }

        add(okButton, "newline 20, span, tag ok");

        setSize(getPreferredSize());
    }

}
