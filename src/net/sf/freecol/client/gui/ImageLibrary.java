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

package net.sf.freecol.client.gui;


import android.util.Log;

import java.util.Map.Entry;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Font;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.GraphicsConfiguration;
import org.freecolandroid.repackaged.java.awt.GraphicsEnvironment;
import org.freecolandroid.repackaged.java.awt.Image;
import org.freecolandroid.repackaged.java.awt.Insets;
import org.freecolandroid.repackaged.java.awt.Transparency;
import org.freecolandroid.repackaged.java.awt.font.TextLayout;
import org.freecolandroid.repackaged.java.awt.image.BufferedImage;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JComponent;


import net.sf.freecol.common.FreeColException;
import net.sf.freecol.common.model.FoundingFather;
import net.sf.freecol.common.model.FreeColGameObjectType;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.Map.Direction;
import net.sf.freecol.common.model.LostCityRumour;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.Ownable;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Tension;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.Role;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.resources.ResourceManager;
import net.sf.freecol.common.util.Xml;


/**
 * Holds various images that can be called upon by others in order to display
 * certain things.
 */
public final class ImageLibrary {

    private static final Logger logger = Logger.getLogger(ImageLibrary.class.getName());

    public static final String UNIT_SELECT = "unitSelect.image",
                               DELETE = "delete.image",
                               PLOWED = "model.improvement.plow.image",
                               TILE_TAKEN = "tileTaken.image",
                               TILE_OWNED_BY_INDIANS = "nativeLand.image",
                               LOST_CITY_RUMOUR = "lostCityRumour.image",
                               DARKNESS = "halo.dark.image";


    /**
     * Draw a (usually small) background image into a (usually larger)
     * space specified by a component, tiling the image to fill up the
     * space.  If the image is not available, just fill with the background
     * colour.
     *
     * @param resource The name of the <code>ImageResource</code> to tile with.
     * @param g The <code>Graphics</code> to draw to.
     * @param c The <code>JComponent</code> that defines the space.
     * @param insets Optional <code>Insets</code> to apply.
     */
    public static void drawTiledImage(String resource, Graphics g,
                                      JComponent c, Insets insets) {
        int width = c.getWidth();
        int height = c.getHeight();
        Image image = ResourceManager.getImage(resource);
        int dx, dy, xmin, ymin;

        if (insets == null) {
            xmin = 0;
            ymin = 0;
        } else {
            xmin = insets.left;
            ymin = insets.top;
            width -= insets.left + insets.right;
            height -= insets.top + insets.bottom;
        }
        if (image != null && (dx = image.getWidth(null)) > 0
            && (dy = image.getHeight(null)) > 0) {
            int xmax, ymax;
            xmax = xmin + width;
            ymax = ymin + height;
            for (int x = xmin; x < xmax; x += dx) {
                for (int y = ymin; y < ymax; y += dy) {
                    g.drawImage(image, x, y, null);
                }
            }
        } else {
            g.setColor(c.getBackground());
            g.fillRect(xmin, ymin, width, height);
        }
    }


    /**
     * The scaling factor used when creating this
     * <code>ImageLibrary</code>. The value
     * <code>1</code> is used if this object is not
     * a result of a scaling operation.
     */
    private final float scalingFactor;

    /**
     * The constructor to use.
     *
     */
    public ImageLibrary() {
        this(1);
    }


    public ImageLibrary(float scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    /**
     * Returns the alarm chip with the given color.
     *
     * @param alarm The alarm level.
     * @param visited a <code>boolean</code> value
     * @param scale a <code>double</code> value
     * @return The alarm chip.
     */
    public Image getAlarmChip(Tension.Level alarm, final boolean visited, double scale) {
        if (visited) {
            return ResourceManager.getChip("alarmChip.visited."
                                           + alarm.toString().toLowerCase(), scale);
        } else {
            return ResourceManager.getChip("alarmChip." + alarm.toString().toLowerCase(), scale);
        }
    }

    /**
     * Returns the beach corner image at the given index.
     *
     * @param index The index of the image to return.
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @return The image at the given index.
     */
    public Image getBeachCornerImage(int index, int x, int y) {
        return ResourceManager.getImage("model.tile.beach.corner" + index
                                        + (isEven(x, y) ? "_even" : "_odd"), scalingFactor);
    }

    /**
     * Returns the beach edge image at the given index.
     *
     * @param index The index of the image to return.
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @return The image at the given index.
     */
    public Image getBeachEdgeImage(int index, int x, int y) {
        return ResourceManager.getImage("model.tile.beach.edge" + index
                                        + (isEven(x, y) ? "_even" : "_odd"), scalingFactor);
    }

    public Image getBonusImage(ResourceType type) {
        return getBonusImage(type, scalingFactor);
    }

    public Image getBonusImage(ResourceType type, double scale) {
        return ResourceManager.getImage(type.getId() + ".image", scale);
    }

    /**
     * Returns the bonus-image for the given tile.
     *
     * @param tile
     * @return the bonus-image for the given tile.
     */
    public Image getBonusImage(Tile tile) {
        if (tile.hasResource()) {
            return getBonusImage(tile.getTileItemContainer().getResource().getType());
        } else {
            return null;
        }
    }

    /**
     * Returns the bonus-ImageIcon at the given index.
     *
     * @param type The type of the bonus-ImageIcon to return.
     * @return <code>ImageIcon</code>
     */
    public ImageIcon getBonusImageIcon(ResourceType type) {
        return new ImageIcon(getBonusImage(type));
    }

    /**
     * Returns the border terrain-image for the given type.
     *
     * @param type The type of the terrain-image to return.
     * @param direction a <code>Direction</code> value
     * @param x The x-coordinate of the location of the tile that is being
     *            drawn.
     * @param y The x-coordinate of the location of the tile that is being
     *            drawn.
     * @return The terrain-image at the given index.
     */
    public Image getBorderImage(TileType type, Direction direction, int x, int y) {
        String key = (type == null) ? "model.tile.unexplored" : type.getId();
        return ResourceManager.getImage(key + ".border_" + direction
                                        + (isEven(x, y) ?  "_even" : "_odd")
                                        + ".image", scalingFactor);
    }


    /**
     * Returns true if the tile with the given coordinates is to be
     * considered "even". This is useful to select different images
     * for the same tile type in order to prevent big stripes or
     * a checker-board effect.
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @return a <code>boolean</code> value
     */
    private boolean isEven(int x, int y) {
        return ((y % 8 <= 2) || ((x + y) % 2 == 0 ));
    }

    /**
     * Returns the coat-of-arms image for the given Nation.
     *
     * @param nation The nation.
     * @return the coat-of-arms of this nation
     */
    public Image getCoatOfArmsImage(Nation nation) {
        return getCoatOfArmsImage(nation, scalingFactor);
    }

    public Image getCoatOfArmsImage(Nation nation, double scale) {
        return ResourceManager.getImage(nation.getId() + ".image", scale);
    }

    /**
     * Returns the coat-of-arms image for the given Nation.
     *
     * @param nation The nation.
     * @return the coat-of-arms of this nation
     */
    public ImageIcon getCoatOfArmsImageIcon(Nation nation) {
        return ResourceManager.getImageIcon(nation.getId() + ".image");
    }

    /**
     * Returns the color of the given player.
     *
     * @param player a <code>Player</code> value
     * @return The color of the given player.
     */
    public Color getColor(Player player) {
        return ResourceManager.getColor(player.getNationID() + ".color");
    }

    /**
     * Returns the color chip with the given color.
     *
     * @param ownable an <code>Ownable</code> value
     * @param scale a <code>double</code> value
     * @return The color chip with the given color.
     */
    public Image getColorChip(Ownable ownable, double scale) {
        return ResourceManager.getChip(ownable.getOwner().getNationID() + ".chip", scale);
    }

    /**
     * Returns the scaled terrain-image for a terrain type (and position 0, 0).
     *
     * @param type The type of the terrain-image to return.
     * @param scale The scale of the terrain image to return.
     * @return The terrain-image
     */
    public Image getCompoundTerrainImage(TileType type, double scale) {
        // Currently used for hills and mountains
        Image terrainImage = getTerrainImage(type, 0, 0, scale);
        Image overlayImage = getOverlayImage(type, 0, 0, scale);
        Image forestImage = type.isForested() ? getForestImage(type, scale) : null;
        if (overlayImage == null && forestImage == null) {
            return terrainImage;
        } else {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
            int width = terrainImage.getWidth(null);
            int height = terrainImage.getHeight(null);
            if (overlayImage != null) {
                height = Math.max(height, overlayImage.getHeight(null));
            }
            if (forestImage != null) {
                height = Math.max(height, forestImage.getHeight(null));
            }
            BufferedImage compositeImage = new BufferedImage(width, height, "0");// gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            Graphics2D g = compositeImage.createGraphics();
            g.drawImage(terrainImage, 0, height - terrainImage.getHeight(null), null);
            if (overlayImage != null) {
                g.drawImage(overlayImage, 0, height - overlayImage.getHeight(null), null);
            }
            if (forestImage != null) {
                g.drawImage(forestImage, 0, height - forestImage.getHeight(null), null);
            }
            g.dispose();
            return compositeImage;
        }
    }


    /**
     * Converts an image to grayscale
     *
     * @param image Source image to convert
     * @return The image in grayscale
     */
    /*
    private ImageIcon convertToGrayscale(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        ColorConvertOp filter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage srcImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        srcImage.createGraphics().drawImage(image, 0, 0, null);
        return new ImageIcon(filter.filter(srcImage, null));
    }
    */

    /**
     * Returns the height of the terrain-image including overlays and
     * forests for the given terrain type.
     *
     * @param type The type of the terrain-image.
     * @return The height of the terrain-image at the given index.
     */
    public int getCompoundTerrainImageHeight(TileType type) {
        int height = getTerrainImageHeight(type);
        if (type != null) {
            Image overlayImage = getOverlayImage(type, 0, 0);
            if (overlayImage != null) {
                height = Math.max(height, overlayImage.getHeight(null));
            }
            if (type.isForested()) {
                height = Math.max(height, getForestImage(type).getHeight(null));
            }
        }
        return height;
    }

    /**
     * Returns the forest image for a terrain type.
     *
     * @param type The type of the terrain-image to return.
     * @return The image at the given index.
     */
    public Image getForestImage(TileType type) {
        return getForestImage(type, scalingFactor);
    }

    public Image getForestImage(TileType type, double scale) {
        return ResourceManager.getImage(type.getId() + ".forest", scale);
    }

    /**
     * Returns the portrait of this Founding Father.
     *
     * @param father a <code>FoundingFather</code> value
     * @return an <code>Image</code> value
     */
    public Image getFoundingFatherImage(FoundingFather father) {
        return ResourceManager.getImage(father.getId() + ".image");
    }

    /**
     * Returns the goods-image at the given index.
     *
     * @param goodsType The type of the goods-image to return.
     * @return The goods-image at the given index.
     */
    public Image getGoodsImage(GoodsType goodsType) {
        return getGoodsImage(goodsType, scalingFactor);
    }

    public Image getGoodsImage(GoodsType goodsType, double scale) {
        return ResourceManager.getImage(goodsType.getId() + ".image", scale);
    }

    /**
     * Returns the goods-image for a goods type.
     *
     * @param goodsType The type of the goods-image to return.
     * @return The goods-image at the given index.
     */
    public ImageIcon getGoodsImageIcon(GoodsType goodsType) {
        return ResourceManager.getImageIcon(goodsType.getId() + ".image");
    }

    public Image getImage(FreeColGameObjectType type) {
        return ResourceManager.getImage(type.getId() + ".image", scalingFactor);
    }

    public Image getImage(FreeColGameObjectType type, double scale) {
        return ResourceManager.getImage(type.getId() + ".image", scale);
    }

    /**
     * Returns the appropriate ImageIcon for Object.
     *
     * @param display The Object to display.
     * @return The appropriate ImageIcon.
     */
    public ImageIcon getImageIcon(Object display, boolean small) {
        Image image = null;
        if (display == null) {
            return new ImageIcon();
        } else if (display instanceof GoodsType) {
            GoodsType goodsType = (GoodsType) display;
            try {
                image = this.getGoodsImage(goodsType);
            } catch (Exception e) {
                logger.warning("could not find image for goods " + goodsType);
            }
        } else if (display instanceof Unit) {
            Unit unit = (Unit) display;
            try {
                image = this.getUnitImageIcon(unit).getImage();
            } catch (Exception e) {
                logger.warning("could not find image for unit " + unit.toString());
            }
        } else if (display instanceof UnitType) {
            UnitType unitType = (UnitType) display;
            try {
                image = this.getUnitImageIcon(unitType).getImage();
            } catch (Exception e) {
                logger.warning("could not find image for unit " + unitType);
            }
        } else if (display instanceof Settlement) {
            Settlement settlement = (Settlement) display;
            try {
                image = this.getSettlementImage(settlement);
            } catch (Exception e) {
                logger.warning("could not find image for settlement " + settlement);
            }
        } else if (display instanceof LostCityRumour) {
            try {
                image = this.getMiscImage(ImageLibrary.LOST_CITY_RUMOUR);
            } catch (Exception e) {
                logger.warning("could not find image for lost city rumour");
            }
        } else if (display instanceof Player) {
            image = this.getCoatOfArmsImage(((Player) display).getNation());
        }
        if (image != null && small) {
            return new ImageIcon(image.getScaledInstance((image.getWidth(null) / 3) * 2,
                                                         (image.getHeight(null) / 3) *2,
                                                         Image.SCALE_SMOOTH));
        } else {
            return (image != null) ? new ImageIcon(image) : null;
        }
    }

    /**
     * Returns the image with the given id.
     *
     * @param id The id of the image to return.
     * @return The image.
     */
    public Image getMiscImage(String id) {
        return getMiscImage(id, scalingFactor);
    }

    public Image getMiscImage(String id, double scale) {
        return ResourceManager.getImage(id, scale);
    }

    /**
     * Returns the image with the given id.
     *
     * @param id The id of the image to return.
     * @return The image.
     */
    public ImageIcon getMiscImageIcon(String id) {
        return new ImageIcon(getMiscImage(id));
    }

    /**
     * Returns the mission chip with the given color.
     *
     * @param ownable an <code>Ownable</code> value
     * @param expertMission Indicates whether or not the missionary is an
     *            expert.
     * @param scale a <code>double</code> value
     * @return The color chip with the given color.
     */
    public Image getMissionChip(Ownable ownable, boolean expertMission, double scale) {
        if (expertMission) {
            return ResourceManager.getChip(ownable.getOwner().getNationID()
                                           + ".mission.expert.chip", scale);
        } else {
            return ResourceManager.getChip(ownable.getOwner().getNationID()
                                           + ".mission.chip", scale);
        }
    }

    /**
     * Returns the monarch-image for the given tile.
     *
     * @param nation The nation this monarch rules.
     * @return the monarch-image for the given nation.
     */
    public Image getMonarchImage(Nation nation) {
        return ResourceManager.getImage(nation.getId() + ".monarch.image");
    }

    /**
     * Returns the monarch-image icon for the given Nation.
     *
     * @param nation The nation this monarch rules.
     * @return the monarch-image for the given nation.
     */
    public ImageIcon getMonarchImageIcon(Nation nation) {
        return ResourceManager.getImageIcon(nation.getId() + ".monarch.image");
    }

    /**
     * Returns the overlay-image for the given type.
     *
     * @param type The type of the terrain-image to return.
     * @param x The x-coordinate of the location of the tile that is being
     *            drawn.
     * @param y The x-coordinate of the location of the tile that is being
     *            drawn.
     * @return The terrain-image at the given index.
     */
    public Image getOverlayImage(TileType type, int x, int y) {
        return getOverlayImage(type, x, y, scalingFactor);
    }

    public Image getOverlayImage(TileType type, int x, int y, double scale) {
        String key = type.getId() + ".overlay" + ((x + y) % 2) + ".image";
        if (ResourceManager.hasResource(key)) {
            return ResourceManager.getImage(key, scale);
        } else {
            return null;
        }
    }

    /**
     * Gets an image to represent the path of the given <code>Unit</code>.
     *
     * @param u The <code>Unit</code>
     * @return The <code>Image</code>.
     */
    public Image getPathImage(Unit u) {
        if (u == null) {
            return null;
        } else {
            return ResourceManager.getImage("path." + getPathType(u) + ".image");
        }
    }

    /**
     * Gets an image to represent the path of the given <code>Unit</code>.
     *
     * @param u The <code>Unit</code>
     * @return The <code>Image</code>.
     */
    public Image getPathNextTurnImage(Unit u) {
        if (u == null) {
            return null;
        } else {
            return ResourceManager.getImage("path." + getPathType(u) + ".nextTurn.image");
        }
    }

    /**
     * Returns the river image at the given index.
     *
     * @param index The index of the image to return.
     * @return The image at the given index.
     */
    public Image getRiverImage(int index) {
        return getRiverImage(index, scalingFactor);
    }

    public Image getRiverImage(int index, double scale) {
        return ResourceManager.getImage("model.tile.river" + index, scale);
    }

    /**
     * Returns the river mouth terrain-image for the direction and magnitude.
     *
     * @param direction a <code>Direction</code> value
     * @param magnitude an <code>int</code> value
     * @param x The x-coordinate of the location of the tile that is being
     *            drawn (ignored).
     * @param y The x-coordinate of the location of the tile that is being
     *            drawn (ignored).
     * @return The terrain-image at the given index.
     */
    public Image getRiverMouthImage(Direction direction, int magnitude, int x, int y) {
        String key = "model.tile.delta_" + direction + (magnitude == 1 ? "_small" : "_large");
        return ResourceManager.getImage(key, scalingFactor);
    }

    public ImageIcon getScaledBonusImageIcon(ResourceType type, float scale) {
        return new ImageIcon(getBonusImage(type, scale));
    }

    /**
     * Returns the scaled goods-ImageIcon for a goods type.
     *
     * @param type The type of the goods-ImageIcon to return.
     * @param scale The scale of the goods-ImageIcon to return.
     * @return The goods-ImageIcon at the given index.
     */
    public ImageIcon getScaledGoodsImageIcon(GoodsType type, double scale) {
        return new ImageIcon(getGoodsImage(type, scale));
    }

    /**
     * Gets a scaled version of this <code>ImageLibrary</code>.
     * @param scalingFactor The factor used when scaling. 2 is twice
     *      the size of the original images and 0.5 is half.
     * @return A new <code>ImageLibrary</code>.
     * @throws FreeColException
     */
    public ImageLibrary getScaledImageLibrary(float scalingFactor) throws FreeColException {
        return new ImageLibrary(scalingFactor);
    }

    /**
     * Returns the scaling factor used when creating this ImageLibrary.
     * @return 1 unless {@link #getScaledImageLibrary} was used to create
     *      this object.
     */
    public float getScalingFactor() {
        return scalingFactor;
    }

    /**
     * Returns the graphics that will represent the given settlement.
     *
     * @param settlement The settlement whose graphics type is needed.
     * @return The graphics that will represent the given settlement.
     */
    public Image getSettlementImage(Settlement settlement) {
        return getSettlementImage(settlement, scalingFactor);
    }

    /**
     * Returns the graphics that will represent the given settlement.
     *
     * @param settlement The settlement whose graphics type is needed.
     * @param scale a <code>double</code> value
     * @return The graphics that will represent the given settlement.
     */
    public Image getSettlementImage(Settlement settlement, double scale) {
        return ResourceManager.getImage(settlement.getImageKey(), scale);
    }

    /**
     * Returns the graphics that will represent the given settlement.
     *
     * @param settlementType The type of settlement whose graphics type is needed.
     * @return The graphics that will represent the given settlement.
     */
    public Image getSettlementImage(SettlementType settlementType) {
        return getSettlementImage(settlementType, scalingFactor);
    }

    public Image getSettlementImage(SettlementType settlementType, double scale) {
        return ResourceManager.getImage(settlementType.getId() + ".image", scale);
    }

    /**
     * Returns the terrain-image for the given type.
     *
     * @param type The type of the terrain-image to return.
     * @param x The x-coordinate of the location of the tile that is being
     *            drawn.
     * @param y The x-coordinate of the location of the tile that is being
     *            drawn.
     * @return The terrain-image at the given index.
     */
    public Image getTerrainImage(TileType type, int x, int y) {
        return getTerrainImage(type, x, y, scalingFactor);
    }

    public Image getTerrainImage(TileType type, int x, int y, double scale) {
        String key = (type == null) ? "model.tile.unexplored" : type.getId();
        Image img = ResourceManager.getImage(key + ".center" + (isEven(x, y) ? "0" : "1")
                + ".image", scale);
        if(img==null){
            Log.d("err",key + ".center" + (isEven(x, y) ? "0" : "1") + ".image not exist!");
        }
        return img;
    }

    /**
     * Returns the height of the terrain-image for a terrain type.
     *
     * @param type The type of the terrain-image.
     * @return The height of the terrain-image at the given index.
     */
    public int getTerrainImageHeight(TileType type) {
        return getTerrainImage(type, 0, 0).getHeight(null);
    }

    /**
     * Returns the width of the terrain-image for a terrain type.
     *
     * @param type The type of the terrain-image.
     * @return The width of the terrain-image at the given index.
     */
    public int getTerrainImageWidth(TileType type) {
        return getTerrainImage(type, 0, 0).getWidth(null);
    }

    /**
     * Returns the ImageIcon that will represent the given unit.
     *
     * @param unit The unit whose graphics type is needed.
     * @return an <code>ImageIcon</code> value
     */
    public ImageIcon getUnitImageIcon(Unit unit) {
        return getUnitImageIcon(unit.getType(), unit.getRole(), unit.hasNativeEthnicity(), false, scalingFactor);
    }

    public ImageIcon getUnitImageIcon(Unit unit, boolean grayscale) {
        return getUnitImageIcon(unit.getType(), unit.getRole(), unit.hasNativeEthnicity(), grayscale, scalingFactor);
    }

    public ImageIcon getUnitImageIcon(Unit unit, boolean grayscale, double scale) {
        return getUnitImageIcon(unit.getType(), unit.getRole(), unit.hasNativeEthnicity(), grayscale, scale);
    }

    public ImageIcon getUnitImageIcon(Unit unit, double scale) {
        return getUnitImageIcon(unit.getType(), unit.getRole(), unit.hasNativeEthnicity(), false, scale);
    }

    /**
     * Returns the ImageIcon that will represent a unit of the given type.
     *
     * @param unitType an <code>UnitType</code> value
     * @return an <code>ImageIcon</code> value
     */
    public ImageIcon getUnitImageIcon(UnitType unitType) {
        return getUnitImageIcon(unitType, Role.DEFAULT, false, false, scalingFactor);
    }

    public ImageIcon getUnitImageIcon(UnitType unitType, boolean grayscale) {
        return getUnitImageIcon(unitType, Role.DEFAULT, false, grayscale, scalingFactor);
    }

    public ImageIcon getUnitImageIcon(UnitType unitType, boolean grayscale, double scale) {
        return getUnitImageIcon(unitType, Role.DEFAULT, false, grayscale, scale);
    }

    public ImageIcon getUnitImageIcon(UnitType unitType, double scale) {
        return getUnitImageIcon(unitType, Role.DEFAULT, false, false, scale);
    }

    public ImageIcon getUnitImageIcon(UnitType unitType, Role role) {
        return getUnitImageIcon(unitType, role, false, false, scalingFactor);
    }


    public ImageIcon getUnitImageIcon(UnitType unitType, Role role, boolean grayscale) {
        return getUnitImageIcon(unitType, role, false, grayscale, scalingFactor);
    }

    /**
     * Returns the ImageIcon that will represent a unit with the given specifics.
     *
     * @param unitType the type of unit to be represented
     * @param role unit has equipment that affects its abilities/appearance
     * @param nativeEthnicity draws the unit with native skin tones
     * @param grayscale draws the icon in an inactive/disabled-looking state
     * @return an <code>ImageIcon</code> value
     */
    public ImageIcon getUnitImageIcon(UnitType unitType, Role role, boolean nativeEthnicity, boolean grayscale, double scale) {
        // units that can only be native don't need the .native key part
        if (unitType.getId().equals("model.unit.indianConvert")
            || unitType.getId().equals("model.unit.brave")) {
            nativeEthnicity = false;
        }
        else for (Entry<String, Boolean> entry : unitType.getAbilitiesRequired().entrySet()) {
            if (entry.getKey().equals("model.ability.native") && entry.getValue() == true) {
                nativeEthnicity = false;
            }
        }
        
        // try to get an image matching the key
        final String key = unitType.getId()
            + (role == Role.DEFAULT ? "" : "." + role.getId())
            + (nativeEthnicity ? ".native" : "")
            + ".image";
        Image image = null;
        if (grayscale) {
            image = ResourceManager.getGrayscaleImage(key, scale);
        } else {
            image = ResourceManager.getImage(key, scale);
        }
        
        if (image == null) {
            // log and attempt fallback
            logger.finest("No image found for image for " + key);
            if (nativeEthnicity == true) {
                // try non-native variant
                return getUnitImageIcon(unitType, role, false, grayscale, scale);
            
            // FIXME: these require the game specification, which ImageLibrary doesn't yet have access to
/*          } else if (role != Role.DEFAULT && !unitType.getId().equals("model.unit.freeColonist")) {
                // try a free colonist with the same role
                unitType = getGame().getSpecification().getUnitType("model.unit.freeColonist");
                return getUnitImageIcon(unitType, role, false, grayscale, scale);
            } else {
                // give up, draw a standard unit icon
                unitType = getGame().getSpecification().getUnitType("model.unit.freeColonist");
                return getUnitImageIcon(unitType, Role.DEFAULT, false, grayscale, scale);
*/          }
            
            logger.warning("Failed to retrieve image for " + key);
            return null;
        }
        return new ImageIcon(image);
    }

    public ImageIcon getUnitImageIcon(UnitType unitType, Role role, boolean grayscale, double scale) {
        return getUnitImageIcon(unitType, role, false, grayscale, scale);
    }

    /**
     * Gets an image to represent the path of the given <code>Unit</code>.
     *
     * @param u The <code>Unit</code>
     * @return The <code>Image</code>.
     *
    private Image getPathIllegalImage(Unit u) {
        if (u == null || u.isNaval()) {
            return (Image) UIManager.get("path.naval.illegal.image");
        } else if (u.isMounted()) {
            return (Image) UIManager.get("path.horse.illegal.image");
        } else if (u.getType() == Unit.WAGON_TRAIN || u.getType() == Unit.TREASURE_TRAIN || u.getType() == Unit.ARTILLERY || u.getType() == Unit.DAMAGED_ARTILLERY) {
            return (Image) UIManager.get("path.wagon.illegal.image");
        } else {
            return (Image) UIManager.get("path.foot.illegal.image");
        }
    }
    */

    public ImageIcon getUnitImageIcon(UnitType unitType, Role role, double scale) {
        return getUnitImageIcon(unitType, role, false, false, scale);
    }

    
    private String getPathType(Unit unit) {
        if (unit.isNaval()) {
            return "naval";
        } else if (unit.isMounted()) {
            return "horse";
        } else if (unit.getType().hasSkill() || unit.isUndead()) {
            return "foot";
        } else {
            return "wagon";
        }
    }
    
    
    /**
     * Create a "chip" with the given text and colors.
     *
     * @param text a <code>String</code> value
     * @param border a <code>Color</code> value
     * @param background a <code>Color</code> value
     * @param foreground a <code>Color</code> value
     * @return an <code>Image</code> value
     */
    public Image createChip(String text, Color border, Color background, Color foreground) {
        // Draw it and put it in the cache
        Font font = ResourceManager.getFont("SimpleFont", Font.BOLD,
                (float) Math.rint(12 * getScalingFactor()));
        // hopefully, this is big enough
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        TextLayout label = new TextLayout(text, font, g2.getFontRenderContext());
        float padding = 6 * getScalingFactor();
        int width = (int) (label.getBounds().getWidth() + padding);
        int height = (int) (label.getAscent() + label.getDescent() + padding);
        g2.setColor(border);
        g2.fillRect(0, 0, width, height);
        g2.setColor(background);
        g2.fillRect(1, 1, width - 2, height - 2);
        g2.setColor(foreground);
        label.draw(g2, (float) (padding/2 - label.getBounds().getX()), label.getAscent() + padding/2);
        g2.dispose();
        return bi.getSubimage(0, 0, width, height);
    }



}
