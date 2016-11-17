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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Graphics;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.Point;
import org.freecolandroid.repackaged.java.awt.Rectangle;
import org.freecolandroid.repackaged.java.awt.event.KeyListener;
import org.freecolandroid.repackaged.java.awt.event.MouseAdapter;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.event.MouseListener;
import org.freecolandroid.repackaged.java.awt.event.MouseMotionListener;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JComponent;
import org.freecolandroid.repackaged.javax.swing.JDesktopPane;
import org.freecolandroid.repackaged.javax.swing.JInternalFrame;
import org.freecolandroid.repackaged.javax.swing.JLayeredPane;
import org.freecolandroid.repackaged.javax.swing.JMenuItem;
import org.freecolandroid.repackaged.javax.swing.SwingUtilities;
import org.freecolandroid.repackaged.javax.swing.border.EmptyBorder;
import org.freecolandroid.repackaged.javax.swing.filechooser.FileFilter;
import org.freecolandroid.repackaged.javax.swing.plaf.basic.BasicInternalFrameUI;

import net.sf.freecol.FreeCol;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.action.FreeColAction;
import net.sf.freecol.client.gui.action.MapControlsAction;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.panel.AboutPanel;
import net.sf.freecol.client.gui.panel.BuildQueuePanel;
import net.sf.freecol.client.gui.panel.CaptureGoodsDialog;
import net.sf.freecol.client.gui.panel.ChatPanel;
import net.sf.freecol.client.gui.panel.ChoiceDialog;
import net.sf.freecol.client.gui.panel.ChoiceItem;
import net.sf.freecol.client.gui.panel.ChooseFoundingFatherDialog;
import net.sf.freecol.client.gui.panel.ClientOptionsDialog;
import net.sf.freecol.client.gui.panel.ColonyPanel;
import net.sf.freecol.client.gui.panel.ColopediaPanel;
import net.sf.freecol.client.gui.panel.CompactLabourReport;
import net.sf.freecol.client.gui.panel.ConfirmDeclarationDialog;
import net.sf.freecol.client.gui.panel.DeclarationDialog;
import net.sf.freecol.client.gui.panel.DifficultyDialog;
import net.sf.freecol.client.gui.panel.DumpCargoDialog;
import net.sf.freecol.client.gui.panel.EditSettlementDialog;
import net.sf.freecol.client.gui.panel.EmigrationPanel;
import net.sf.freecol.client.gui.panel.ErrorPanel;
import net.sf.freecol.client.gui.panel.EuropePanel;
import net.sf.freecol.client.gui.panel.EventPanel;
import net.sf.freecol.client.gui.panel.FindSettlementDialog;
import net.sf.freecol.client.gui.panel.FreeColDialog;
import net.sf.freecol.client.gui.panel.FreeColPanel;
import net.sf.freecol.client.gui.panel.GameOptionsDialog;
import net.sf.freecol.client.gui.panel.IndianSettlementPanel;
import net.sf.freecol.client.gui.panel.LabourData.UnitData;
import net.sf.freecol.client.gui.panel.LoadingSavegameDialog;
import net.sf.freecol.client.gui.panel.MainPanel;
import net.sf.freecol.client.gui.panel.MapControls;
import net.sf.freecol.client.gui.panel.MapGeneratorOptionsDialog;
import net.sf.freecol.client.gui.panel.MonarchPanel;
import net.sf.freecol.client.gui.panel.NegotiationDialog;
import net.sf.freecol.client.gui.panel.PreCombatDialog;
import net.sf.freecol.client.gui.panel.RecruitDialog;
import net.sf.freecol.client.gui.panel.ReportCargoPanel;
import net.sf.freecol.client.gui.panel.ReportColonyPanel;
import net.sf.freecol.client.gui.panel.ReportContinentalCongressPanel;
import net.sf.freecol.client.gui.panel.ReportEducationPanel;
import net.sf.freecol.client.gui.panel.ReportExplorationPanel;
import net.sf.freecol.client.gui.panel.ReportForeignAffairPanel;
import net.sf.freecol.client.gui.panel.ReportHighScoresPanel;
import net.sf.freecol.client.gui.panel.ReportHistoryPanel;
import net.sf.freecol.client.gui.panel.ReportIndianPanel;
import net.sf.freecol.client.gui.panel.ReportLabourPanel;
import net.sf.freecol.client.gui.panel.ReportMilitaryPanel;
import net.sf.freecol.client.gui.panel.ReportNavalPanel;
import net.sf.freecol.client.gui.panel.ReportProductionPanel;
import net.sf.freecol.client.gui.panel.ReportReligiousPanel;
import net.sf.freecol.client.gui.panel.ReportRequirementsPanel;
import net.sf.freecol.client.gui.panel.ReportTradePanel;
import net.sf.freecol.client.gui.panel.ReportTurnPanel;
import net.sf.freecol.client.gui.panel.RiverStylePanel;
import net.sf.freecol.client.gui.panel.SelectAmountDialog;
import net.sf.freecol.client.gui.panel.SelectDestinationDialog;
import net.sf.freecol.client.gui.panel.ServerListPanel;
import net.sf.freecol.client.gui.panel.StartGamePanel;
import net.sf.freecol.client.gui.panel.StatisticsPanel;
import net.sf.freecol.client.gui.panel.StatusPanel;
import net.sf.freecol.client.gui.panel.TilePanel;
import net.sf.freecol.client.gui.panel.TradeRouteDialog;
import net.sf.freecol.client.gui.panel.TradeRouteInputDialog;
import net.sf.freecol.client.gui.panel.TrainDialog;
import net.sf.freecol.client.gui.panel.VictoryPanel;
import net.sf.freecol.client.gui.panel.WarehouseDialog;
import net.sf.freecol.client.gui.panel.WorkProductionPanel;
import net.sf.freecol.common.ServerInfo;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.DiplomaticTrade;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.FoundingFather;
import net.sf.freecol.common.model.FreeColGameObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.IndianNationType;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Location;
import net.sf.freecol.common.model.ModelMessage;
import net.sf.freecol.common.model.Monarch.MonarchAction;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.WorkLocation;
import net.sf.freecol.common.option.Option;
import net.sf.freecol.common.option.OptionGroup;
import android.widget.Toast;


/**
 * The main container for the other GUI components in FreeCol. This container is
 * where the panels, dialogs and menus are added. In addition, this is the
 * component in which the map graphics are displayed. <br>
 * <b>Displaying panels and a dialogs</b> <br>
 * <br>
 * <code>Canvas</code> contains methods to display various panels and dialogs.
 * Most of these methods use {@link net.sf.freecol.client.gui.i18n i18n} to get
 * localized text. Here is an example: <br>
 *
 * <PRE>
 *
 * if (canvas.showConfirmDialog("choice.text", "choice.yes", "choice.no")) { //
 * DO SOMETHING. }
 *
 * </PRE>
 *
 * <br>
 * where "choice.text", "choice.yes" and "choice.no" are keys for a localized
 * message. See {@link net.sf.freecol.client.gui.i18n i18n} for more
 * information. <br>
 * <b>The difference between a panel and a dialog</b> <br>
 * <br>
 * When displaying a dialog, using a <code>showXXXDialog</code>, the calling
 * thread will wait until that dialog is dismissed before returning. In
 * contrast, a <code>showXXXPanel</code>-method returns immediately.
 */
public final class Canvas extends JDesktopPane {

    public static enum BoycottAction {
        CANCEL,
        PAY_ARREARS,
        DUMP_CARGO
    }

    public static enum BuyAction {
        CANCEL,
        BUY,
        HAGGLE
    }

    public static enum ClaimAction {
        CANCEL,
        ACCEPT,
        STEAL
    }

    public static enum EventType {
        FIRST_LANDING,
        MEETING_NATIVES,
        MEETING_EUROPEANS,
        MEETING_AZTEC,
        MEETING_INCA,
        DISCOVER_PACIFIC
    }

    public static enum MissionaryAction {
        CANCEL,
        ESTABLISH_MISSION,
        DENOUNCE_HERESY,
        INCITE_INDIANS
    }

    public static enum PopupPosition {
        ORIGIN,
        CENTERED,
        CENTERED_LEFT,
        CENTERED_RIGHT,
    }

    public static enum ScoutColonyAction {
        CANCEL,
        FOREIGN_COLONY_NEGOTIATE,
        FOREIGN_COLONY_SPY,
        FOREIGN_COLONY_ATTACK
    }

    public static enum ScoutIndianSettlementAction {
        CANCEL,
        INDIAN_SETTLEMENT_SPEAK,
        INDIAN_SETTLEMENT_TRIBUTE,
        INDIAN_SETTLEMENT_ATTACK
    }

    public static enum SellAction {
        CANCEL,
        SELL,
        HAGGLE,
        GIFT
    }

    public static enum TradeAction {
        CANCEL,
        BUY,
        SELL,
        GIFT
    }

    /**
     * Handles the moving of internal frames.
     */
    class FrameMotionListener extends MouseAdapter implements MouseMotionListener {

        private JInternalFrame f;

        private Point loc = null;


        FrameMotionListener(JInternalFrame f) {
            this.f = f;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (loc == null || f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                return;
            }

            Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
            int moveX = loc.x - p.x;
            int moveY = loc.y - p.y;
            f.getDesktopPane().getDesktopManager().dragFrame(f, f.getX() - moveX, f.getY() - moveY);
            loc = p;
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                return;
            }
            loc = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
            f.getDesktopPane().getDesktopManager().beginDraggingFrame(f);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (loc == null || f.getDesktopPane() == null || f.getDesktopPane().getDesktopManager() == null) {
                return;
            }
            f.getDesktopPane().getDesktopManager().endDraggingFrame(f);
        }
    }

    /**
     * A class for frames being used as tool boxes.
     */
    class ToolBoxFrame extends JInternalFrame {

    }

    private static final Logger logger = Logger.getLogger(Canvas.class.getName());

    private static final Integer MAIN_LAYER = JLayeredPane.DEFAULT_LAYER;

    private static final Integer STATUS_LAYER = JLayeredPane.POPUP_LAYER;

    /**
     * To save the most recently open dialog in Europe
     * (<code>RecruitDialog</code>, <code>PurchaseDialog</code>, <code>TrainDialog</code>)
     */
    private FreeColDialog<Integer> europeOpenDialog = null;

    private final FreeColClient freeColClient;

    private GUI gui;

    private MainPanel mainPanel;

    private final StartGamePanel startGamePanel;

    private final EuropePanel europePanel;

    private final StatusPanel statusPanel;

    private final ChatPanel chatPanel;

    private final MapViewer mapViewer;

    private final ServerListPanel serverListPanel;

    private final ClientOptionsDialog clientOptionsDialog;

    private final LoadingSavegameDialog loadingSavegameDialog;



    private boolean clientOptionsDialogShowing = false;

    private MapControls mapControls;

    /**
     * Variable used for detecting resizing.
     */
    private Dimension oldSize = null;

    private Dimension initialSize = null;


    /**
     * The constructor to use.
     *
     * @param client main control class.
     * @param size The bounds of this <code>Canvas</code>.
     * @param mapViewer The object responsible of drawing the map onto this component.
     */
    public Canvas(FreeColClient client, GUI gui, Dimension size, MapViewer mapViewer) {
        this.freeColClient = client;
        this.gui = gui;
        this.mapViewer = mapViewer;

        this.initialSize = size;

        setLocation(0, 0);
        setSize(size);

        setDoubleBuffered(true);
        setOpaque(false);
        setLayout(null);

        startGamePanel = new StartGamePanel(freeColClient, gui);
        serverListPanel = new ServerListPanel(freeColClient, gui, freeColClient.getConnectController());
        europePanel = new EuropePanel(freeColClient, gui, this);
        statusPanel = new StatusPanel(freeColClient, gui);
        chatPanel = new ChatPanel(freeColClient, gui);
        clientOptionsDialog = new ClientOptionsDialog(freeColClient, gui);
        loadingSavegameDialog = new LoadingSavegameDialog(freeColClient, gui);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        // takeFocus();

        // chatDisplayThread = new ChatDisplayThread();
        // chatDisplayThread.start();

        // TODO: move shutdown hook from GUI to (say) client!
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(FreeCol.CLIENT_THREAD+"Quitting Game") {
            @Override
            public void run() {
                freeColClient.getConnectController().quitGame(true);
            }
        });

        createKeyBindings();

        logger.info("Canvas created.");
    }

    /**
     * Adds a component to this Canvas.
     *
     * @param comp The component to add
     * @return The component argument.
     */
    @Override
    public Component add(Component comp) {
        add(comp, null);
        return comp;
    }

    /**
     * Adds a component to this Canvas. Removes the statuspanel if visible (and
     * <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this ToEuropePanel.
     * @param i The layer to add the component to (see JLayeredPane).
     */
    public void add(Component comp, Integer i) {
        add(comp, i, true);
    }

    /**
     * Adds a component to this Canvas. Removes the statuspanel if visible (and
     * <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this ToEuropePanel.
     * @param i The layer to add the component to (see JLayeredPane).
     */
    public void add(Component comp, Integer i, boolean update) {

        if (comp != statusPanel && !(comp instanceof JMenuItem) && !(comp instanceof FreeColDialog<?>)
                && statusPanel.isVisible()) {
            remove(statusPanel, false);
        }

        try {
            if (i == null) {
                super.add(comp);
            } else {
                super.add(comp, i);
            }
        } catch(Exception e) {
            logger.warning("add component failed with layer " + i);
            e.printStackTrace();
        }

        if (update) {
            gui.updateMenuBar();
            freeColClient.getActionManager().update();
        }
    }

    /**
     * Adds a component centered on this Canvas inside a frame. Removes the
     * statuspanel if visible (and <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this JInternalFrame.
     * @return The <code>JInternalFrame</code> that was created and added.
     */
    public JInternalFrame addAsFrame(JComponent comp) {
        return addAsFrame(comp, false, PopupPosition.CENTERED);
    }


    /**
     * Adds a component centered on this Canvas inside a frame.
     * The frame is considered as a tool box (not counted as a frame
     * by the methods deciding if a panel is being displayed).
     *
     * <br><br>
     *
     * Removes the statuspanel if visible (and
     * <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this JInternalFrame.
     * @return The <code>JInternalFrame</code> that was created and added.
     */
    public JInternalFrame addAsToolBox(JComponent comp) {
        return addAsFrame(comp, true, PopupPosition.CENTERED);
    }

    /**
     * Closes the {@link MainPanel}.
     */
    public void closeMainPanel() {
       if (mainPanel != null) {
          remove(mainPanel);
          mainPanel = null;
       }
    }


    /**
     * Checks if this <code>Canvas</code> contains any ingame components.
     *
     * @return <code>true</code> if there is a single ingame component.
     */
    public boolean containsInGameComponents() {
        KeyListener[] keyListeners = getKeyListeners();
        if (keyListeners.length > 0) {
            return true;
        }

        MouseListener[] mouseListeners = getMouseListeners();
        if (mouseListeners.length > 0) {
            return true;
        }

        MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
        if (mouseMotionListeners.length > 0) {
            return true;
        }

        return false;
    }

    /**
     * Detailed view of a foreign colony when in debug mode.
     *
     * @param settlement The <code>Settlement</code> with the colony
     */
    public void debugForeignColony(Settlement settlement) {
        if (settlement instanceof Colony) {
            FreeColGameObject fcgo = freeColClient.getFreeColServer().getGame()
                .getFreeColGameObject(settlement.getId());
            gui.showColonyPanel((Colony) fcgo);
        }
    }

    /**
     * Gets the <code>LoadingSavegameDialog</code>.
     *
     * @return The <code>LoadingSavegameDialog</code>.
     */
    public LoadingSavegameDialog getLoadingSavegameDialog() {
        return loadingSavegameDialog;
    }

    /**
     * Returns the MapControls of this Canvas.
     *
     * @return a <code>MapControls</code> value
     */
    public MapControls getMapControls() {
        return mapControls;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(640, 480);
    }

    @Override
    public Dimension getPreferredSize() {
        return initialSize;
    }

    /**
     * The any panel this <code>Canvas</code> is displaying.
     *
     * @return A <code>Component</code> the <code>Canvas</code> is
     *         displaying, or null if none found.
     */
    public Component getShowingSubPanel() {
        Component[] components = getComponents();
        for (Component c : components) {
            if (c instanceof ToolBoxFrame) {
                continue;
            }
            if (c instanceof JInternalFrame) {
                return c;
            } else if (c instanceof JInternalFrame.JDesktopIcon) {
                return c;
            }
        }
        return null;
    }

    /**
     * Checks if the <code>ClientOptionsDialog</code> is visible.
     *
     * @return <code>true</code> if no internal frames are open.
     */
    public boolean isClientOptionsDialogShowing() {
        return clientOptionsDialogShowing;
    }

    /**
     * Checks if mapboard actions should be enabled.
     *
     * @return <code>true</code> if no internal frames are open.
     */
    public boolean isMapboardActionsEnabled() {
        return !isShowingSubPanel();
    }

    /**
     * Checks if this <code>Canvas</code> displaying another panel.
     * <p>
     * Note that the previous implementation could throw exceptions
     * in some cases, thus the change.
     *
     * @return <code>true</code> if the <code>Canvas</code> is displaying an
     *         internal frame.
     */
    public boolean isShowingSubPanel() {
        return getShowingSubPanel() != null;
    }

    /**
     * Paints this component. This method will use {@link MapViewer#display} to draw
     * the map/background on this component.
     *
     * @param g The Graphics context in which to draw this component.
     * @see MapViewer#display
     */
    @Override
    public void paintComponent(Graphics g) {
        updateSizes();
        Graphics2D g2d = (Graphics2D) g;
        mapViewer.display(g2d);
    }

    public void refreshPlayersTable() {
//        startGamePanel.refreshPlayersTable();

    }


    /**
     * Removes the given component from this Container.
     *
     * @param comp The component to remove from this Container.
     */
    @Override
    public void remove(Component comp) {
        remove(comp, true);
    }

    /**
     * Removes the given component from this Container.
     *
     * @param comp The component to remove from this Container.
     * @param update The <code>Canvas</code> will be enabled, the graphics
     *            repainted and both the menubar and the actions will be updated
     *            if this parameter is <code>true</code>.
     */
    public void remove(Component comp, boolean update) {
        if (comp == null) {
            return;
        } else if (comp instanceof FreeColPanel) {
            ((FreeColPanel) comp).notifyClose();
        }

        final Rectangle updateBounds = comp.getBounds();
        final JInternalFrame frame = getInternalFrame(comp);
        if (frame != null && frame != comp) {
            // updateBounds = frame.getBounds();
            frame.dispose();
        } else {
            super.remove(comp);
        }

        repaint(updateBounds.x, updateBounds.y, updateBounds.width, updateBounds.height);

        final boolean takeFocus = (comp != statusPanel);
        if (update) {
            gui.updateMenuBar();
            freeColClient.getActionManager().update();
            if (takeFocus && !isShowingSubPanel()) {
                requestFocus();
            }
        }
    }

    /**
     * Removes components that is only used when in game.
     */
    public void removeInGameComponents() {
        // remove listeners, they will be added when launching the new game...
//        KeyListener[] keyListeners = getKeyListeners();
//        for (int i = 0; i < keyListeners.length; ++i) {
//            removeKeyListener(keyListeners[i]);
//        }
//
//        MouseListener[] mouseListeners = getMouseListeners();
//        for (int i = 0; i < mouseListeners.length; ++i) {
//            removeMouseListener(mouseListeners[i]);
//        }
//
//        MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
//        for (int i = 0; i < mouseMotionListeners.length; ++i) {
//            removeMouseMotionListener(mouseMotionListeners[i]);
//        }

        // change to default view mode
        // Must be done before removing jMenuBar to prevent exception (crash)
        gui.getMapViewer().getViewMode().changeViewMode(ViewMode.MOVE_UNITS_MODE);

//        for (Component c : getComponents()) {
//            remove(c, false);
//        }

    }

    /**
     * Closes all panels, changes the background and shows the main menu.
     */
    public void returnToTitle() {
        // TODO: check if the GUI object knows that we're not
        // inGame. (Retrieve value of GUI::inGame.)  If GUI thinks
        // we're still in the game then log an error because at this
        // point the GUI should have been informed.
        removeInGameComponents();

        gui.showMainPanel();
        gui.playSound("sound.intro.general");
        repaint();
    }

    public void showAboutPanel() {
        showSubPanel(new AboutPanel(freeColClient, gui));
    }



    /**
     * Displays a dialog that asks the user what he wants to do with his armed
     * unit in a native settlement.
     *
     * @param settlement The <code>IndianSettlement</code> to consider.
     * @return The chosen action, tribute, attack or cancel.
     */
    public ScoutIndianSettlementAction showArmedUnitIndianSettlementDialog(IndianSettlement settlement) {
        List<ChoiceItem<ScoutIndianSettlementAction>> choices
            = new ArrayList<ChoiceItem<ScoutIndianSettlementAction>>();
        choices.add(new ChoiceItem<ScoutIndianSettlementAction>(
                Messages.message("scoutSettlement.tribute"),
                ScoutIndianSettlementAction.INDIAN_SETTLEMENT_TRIBUTE));
        choices.add(new ChoiceItem<ScoutIndianSettlementAction>(
                Messages.message("scoutSettlement.attack"),
                ScoutIndianSettlementAction.INDIAN_SETTLEMENT_ATTACK));
        String messageId = settlement.getAlarmLevelMessageId(freeColClient.getMyPlayer());
        ScoutIndianSettlementAction result
            = showChoiceDialog(settlement.getTile(),
                               Messages.message(StringTemplate.template(messageId)
                                                .addStringTemplate("%nation%", settlement.getOwner().getNationName())),
                               Messages.message("cancel"),
                               choices);
        return (result == null) ? ScoutIndianSettlementAction.CANCEL : result;
    }

    /**
     * Displays a dialog that asks the user whether to pay arrears for
     * boycotted goods or to dump them instead.
     *
     * @param goods a <code>Goods</code> value
     * @param europe an <code>Europe</code> value
     * @return a <code>boolean</code> value
     */
    public BoycottAction showBoycottedGoodsDialog(Goods goods, Europe europe) {
        int arrears = europe.getOwner().getArrears(goods.getType());
        List<ChoiceItem<BoycottAction>> choices
            = new ArrayList<ChoiceItem<BoycottAction>>();
        choices.add(new ChoiceItem<BoycottAction>(
                Messages.message("boycottedGoods.payArrears"),
                BoycottAction.PAY_ARREARS));
        choices.add(new ChoiceItem<BoycottAction>(
                Messages.message("boycottedGoods.dumpGoods"),
                BoycottAction.DUMP_CARGO));
        BoycottAction result =
            showChoiceDialog(null,
                             Messages.message(StringTemplate.template("boycottedGoods.text")
                                              .add("%goods%", goods.getNameKey())
                                              .add("%europe%", europe.getNameKey())
                                              .addAmount("%amount%", arrears)),
                Messages.message("cancel"),
                choices);
        return (result == null) ? BoycottAction.CANCEL : result;
    }

    public void showBuildQueuePanel(Colony colony) {
        showSubPanel(new BuildQueuePanel(freeColClient, gui, colony));

    }

    public void showBuildQueuePanel(Colony colony, Runnable callBack) {
        FreeColPanel panel = new BuildQueuePanel(freeColClient, gui, colony);
        panel.addClosingCallback(callBack);
        showSubPanel(panel);

    }

    /**
     * Displays the panel for negotiating a purchase from a settlement.
     *
     * @param unit The <code>Unit</code> that is buying.
     * @param settlement The <code>Settlement</code> to buy from.
     * @param goods The <code>Goods</code> to buy.
     * @param gold The current negotiated price.
     * @param canBuy True if buy is a valid option.
     * @return The chosen action, buy, haggle, or cancel.
     */
    public BuyAction showBuyDialog(Unit unit, Settlement settlement,
                                   Goods goods, int gold, boolean canBuy) {
        StringTemplate goodsTemplate
            = StringTemplate.template("model.goods.goodsAmount")
            .add("%goods%", goods.getType().getNameKey())
            .addAmount("%amount%", goods.getAmount());
        StringTemplate nation = settlement.getOwner().getNationName();
        List<ChoiceItem<BuyAction>> choices
            = new ArrayList<ChoiceItem<BuyAction>>();
        choices.add(new ChoiceItem<BuyAction>(
                Messages.message("buy.takeOffer"),
                BuyAction.BUY, canBuy));
        choices.add(new ChoiceItem<BuyAction>(
                Messages.message("buy.moreGold"),
                BuyAction.HAGGLE));
        BuyAction result = showChoiceDialog(unit.getTile(),
                Messages.message(StringTemplate.template("buy.text")
                                 .addStringTemplate("%nation%", nation)
                                 .addStringTemplate("%goods%", goodsTemplate)
                                 .addAmount("%gold%", gold)),
                Messages.message("buyProposition.cancel"),
                choices);
        return (result == null) ? BuyAction.CANCEL : result;
    }

    /**
     * Displays the <code>LootCargoDialog</code>.
     *
     * @param winner The <code>Unit</code> that is looting.
     * @param loot list of <code>Goods</code> to select from
     * @return list of <code>Goods</code> to loot
     */
    public List<Goods> showCaptureGoodsDialog(Unit winner, List<Goods> loot) {
        CaptureGoodsDialog dialog = new CaptureGoodsDialog(freeColClient, gui, winner, loot);
        return showFreeColDialog(dialog, winner.getTile());
    }


    /**
     * Displays the <code>ChatPanel</code>.
     *
     * @see ChatPanel
     */
    public void showChatPanel() {
        // TODO: does it have state, or can we create a new one?
        if (freeColClient.isSingleplayer()) {
            return; // In single player, no chat available
        }
        showSubPanel(chatPanel);
    }

    /**
     * Displays a dialog with a text and a cancel-button, in addition
     * to buttons for each of the objects returned for the given list.
     *
     * @param tile An optional tile to make visible (not under the dialog).
     * @param text The text that explains the choice for the user.
     * @param cancelText The text displayed on the "cancel"-button.
     * @param choices The <code>List</code> containing the ChoiceItems to
     *            create buttons for.
     * @return The chosen object, or <i>null</i> for the cancel-button.
     */
    public <T> T showChoiceDialog(Tile tile, String text, String cancelText,
                                  List<ChoiceItem<T>> choices) {
        FreeColDialog<ChoiceItem<T>> choiceDialog
            = FreeColDialog.createChoiceDialog(freeColClient, gui, text, cancelText, choices);
        if (choiceDialog.getHeight() > getHeight() / 3) {
            choiceDialog.setSize(choiceDialog.getWidth(), (getHeight() * 2) / 3);
        }
        ChoiceItem<T> response = showFreeColDialog(choiceDialog, tile);
        return (response == null) ? null : response.getObject();
    }

    public MonarchAction showChoiceMonarchActionDialog(String monarchTitle, List<ChoiceItem<MonarchAction>> actions) {
        ChoiceDialog<MonarchAction> choiceDialog= new ChoiceDialog<MonarchAction>(freeColClient, gui, monarchTitle,
                                          "Cancel", actions);
        return showFreeColDialog(choiceDialog);
    }


    public FoundingFather showChooseFoundingFatherDialog(List<ChoiceItem<FoundingFather>> fathers, String fatherTitle) {
        ChoiceDialog<FoundingFather> choiceDialog
        = new ChoiceDialog<FoundingFather>(freeColClient, gui, fatherTitle, "Cancel",
            fathers);
        return showFreeColDialog(choiceDialog);
    }

    public FoundingFather showChooseFoundingFatherDialog(List<FoundingFather> ffs) {

        return showFreeColDialog(new ChooseFoundingFatherDialog(freeColClient, gui, ffs));
    }

    /**
     * Display the panel for claiming land.
     *
     * @param tile The <code>Tile</code> to claim.
     * @param player The <code>Player</code> that is claiming.
     * @param price An asking price, if any.
     * @param owner The <code>Player</code> that owns the land.
     * @param canAccept True if accept is a valid option.
     * @return The chosen action, accept, steal or cancel.
     */
    public ClaimAction showClaimDialog(Tile tile, Player player, int price,
                                       Player owner, boolean canAccept) {
        List<ChoiceItem<ClaimAction>> choices
            = new ArrayList<ChoiceItem<ClaimAction>>();
        choices.add(new ChoiceItem<ClaimAction>(Messages.message(StringTemplate.template("indianLand.pay")
                                                                 .addAmount("%amount%", price)),
                ClaimAction.ACCEPT, canAccept));
        choices.add(new ChoiceItem<ClaimAction>(
                Messages.message("indianLand.take"),
                ClaimAction.STEAL));
        ClaimAction result =
            showChoiceDialog(tile,
                             Messages.message(StringTemplate.template("indianLand.text")
                                              .addStringTemplate("%player%", owner.getNationName())),
                             Messages.message("indianLand.cancel"),
                             choices);
        return (result == null) ? ClaimAction.CANCEL : result;
    }

    /**
     * Displays a dialog for setting client options.
     *
     * @return <code>true</code> if the client options have been modified, and
     *         <code>false</code> otherwise.
     */
    public OptionGroup showClientOptionsDialog() {
        clientOptionsDialog.initialize();
        clientOptionsDialogShowing = true;
        OptionGroup group = showFreeColDialog(clientOptionsDialog);
        clientOptionsDialogShowing = false;
        freeColClient.getActionManager().update();
        return group;
    }

    public void showColonyPanel(Colony colony, Runnable callback) {
        FreeColPanel panel = new ColonyPanel(freeColClient, gui, colony);
        panel.addClosingCallback(callback);
        showSubPanel(panel);
    }

    /**
     * Describe <code>showColonyPanel</code> method here.
     *
     * @param t a <code>Tile</code> value
     */
    public void showColonyPanel(Tile t) {
        if (gui.getCurrentViewMode() == ViewMode.MOVE_UNITS_MODE) {
            if (t != null && t.getColony() != null
                && freeColClient.getMyPlayer().owns(t.getColony())) {
                mapViewer.setFocus(t);
                mapViewer.stopBlinking();
                gui.showColonyPanel(t.getColony());
            }
        }
    }


    public void showColopediaPanel(String nodeId) {
        showSubPanel(new ColopediaPanel(freeColClient, gui, nodeId));
    }


    public void showCompactLabourReport() {
        showSubPanel(new CompactLabourReport(freeColClient, gui));

    }

    public void showCompactLabourReport(UnitData unitData) {
        CompactLabourReport details = new CompactLabourReport(freeColClient, gui, unitData);
        details.initialize();
        addAsFrame(details);
        details.requestFocus();
    }

    /**
     * Display a dialog to confirm a declaration of independence.
     *
     * @return A list of names for a new nation.
     */
    public List<String> showConfirmDeclarationDialog() {
        return showFreeColDialog(new ConfirmDeclarationDialog(freeColClient, gui));
    }

    /**
     * Display a dialog following declaration of independence.
     */
    public void showDeclarationDialog() {
        showFreeColDialog(new DeclarationDialog(freeColClient, gui));
    }

    public void showDifficultyDialog() {
        showSubPanel(new DifficultyDialog(freeColClient, gui, freeColClient.getGame().getDifficultyLevel()));
    }

    public OptionGroup showDifficultyDialog(Specification specification) {
        return showFreeColDialog(new DifficultyDialog(freeColClient, gui, specification));
    }

    /**
     * Displays the <code>DumpCargoDialog</code>.
     *
     * @param unit The <code>Unit</code> that is dumping.
     * @return A list of <code>Goods</code> to dump.
     */
    public List<Goods> showDumpCargoDialog(Unit unit) {
        DumpCargoDialog dumpDialog = new DumpCargoDialog(freeColClient, gui, unit);
        return showFreeColDialog(dumpDialog, unit.getTile());
    }

    public void showEditSettlementDialog(IndianSettlement settlement) {
        showFreeColDialog(new EditSettlementDialog(freeColClient, gui, settlement));
    }

    /**
     * Shows the panel that allows the user to choose which unit will emigrate
     * from Europe. This method may only be called if the user has William
     * Brewster in congress.
     *
     * @param fountainOfYouth a <code>boolean</code> value
     * @return The emigrant that was chosen by the user.
     */
    public int showEmigrationPanel(boolean fountainOfYouth) {
        EmigrationPanel emigrationPanel = new EmigrationPanel(freeColClient, gui);
        emigrationPanel.initialize(freeColClient.getMyPlayer().getEurope(), fountainOfYouth);
        return showFreeColDialog(emigrationPanel);
    }


    /**
     * Displays one of the Europe Dialogs for Recruit, Purchase, Train.
     * Closes any currently open Dialogs.
     * Does not return from this method before the panel is closed.
     *
     * @param europeAction the type of panel to display
     * @return <code>FreeColDialog.getResponseInt</code>.
     */
    public int showEuropeDialog(EuropePanel.EuropeAction europeAction) {
        // Close any open Europe Dialog (Recruit, Purchase, Train)
        try {
            if (europeOpenDialog != null) {
                europeOpenDialog.setResponse(new Integer(-1));
            }
        } catch (NumberFormatException e) {
            logger.warning("Canvas.showEuropeDialog: Invalid europeDialogType");
        }

        FreeColDialog<Integer> localDialog = null;

        // Open new Dialog
        switch (europeAction) {
        case EXIT:
        case UNLOAD:
        case SAIL:
            return -1;
        case RECRUIT:
            localDialog = new RecruitDialog(freeColClient, gui);
            break;
        case PURCHASE:
        case TRAIN:
            localDialog = new TrainDialog(freeColClient, gui, europeAction);
            break;
        }
        localDialog.initialize();
        europeOpenDialog = localDialog; // Set the open dialog to the class variable

        int response = showFreeColDialog(localDialog);

        if (europeOpenDialog == localDialog) {
            europeOpenDialog = null;    // Clear class variable when it's closed
        }

        return response;
    }

    /**
     * Displays the <code>EuropePanel</code>.
     *
     * @see EuropePanel
     */
    public void showEuropePanel() {
        if (freeColClient.getGame() == null) {
            gui.errorMessage("europe.noGame");
        } else {
            europePanel.initialize(freeColClient.getMyPlayer().getEurope(), freeColClient.getGame());
            showSubPanel(europePanel);
        }
    }

    /**
     * Display an event panel.
     *
     * @param type The <code>EventType</code>.
     */
    public void showEventPanel(EventType type) {
        showSubPanel(new EventPanel(freeColClient, gui, type), PopupPosition.CENTERED);
    }

    public void showFindSettlementDialog() {
        showSubPanel(new FindSettlementDialog<Canvas>(freeColClient, gui), PopupPosition.ORIGIN);
    }

    /**
     * Displays the given dialog.
     *
     * @param freeColDialog The dialog to be displayed
     * @return The {@link FreeColDialog#getResponse reponse} returned by
     *         the dialog.
     */
    public <T> T showFreeColDialog(FreeColDialog<T> freeColDialog) {
        return showFreeColDialog(freeColDialog, null);
    }

    public void showGameOptionsDialog(boolean editable, boolean loadCustomOptions) {
        showSubPanel(new GameOptionsDialog(freeColClient, gui, editable, loadCustomOptions));
    }

    /**
     * Displays the high scores panel.
     *
     * @param messageId An optional message to add to the high scores panel.
     */
    public void showHighScoresPanel(String messageId) {
        showSubPanel(new ReportHighScoresPanel(freeColClient, gui, messageId), PopupPosition.ORIGIN);
    }


    // A variety of special purpose panels/dialogs follow

    /**
     * Displays the panel of the given native settlement.
     *
     * @param indianSettlement The <code>IndianSettlement</code> to display.
     */
    public void showIndianSettlementPanel(IndianSettlement indianSettlement) {
        IndianSettlementPanel panel
            = new IndianSettlementPanel(freeColClient, gui, indianSettlement);
        showFreeColPanel(panel, indianSettlement.getTile());
    }

    /**
     * Displays the panel for trading with an <code>IndianSettlement</code>.
     *
     * @param settlement The native settlement to trade with.
     * @param canBuy Show a "buy" option.
     * @param canSell Show a "sell" option.
     * @param canGift Show a "gift" option.
     * @return The chosen action, buy, sell, gift or cancel.
     */
    public TradeAction showIndianSettlementTradeDialog(Settlement settlement,
                                                       boolean canBuy,
                                                       boolean canSell,
                                                       boolean canGift) {
        ArrayList<ChoiceItem<TradeAction>> choices
            = new ArrayList<ChoiceItem<TradeAction>>();
        choices.add(new ChoiceItem<TradeAction>(
                Messages.message("tradeProposition.toBuy"),
                TradeAction.BUY, canBuy));
        choices.add(new ChoiceItem<TradeAction>(
                Messages.message("tradeProposition.toSell"),
                TradeAction.SELL, canSell));
        choices.add(new ChoiceItem<TradeAction>(
                Messages.message("tradeProposition.toGift"),
                TradeAction.GIFT, canGift));
        TradeAction result =
            showChoiceDialog(settlement.getTile(),
                             Messages.message(StringTemplate.template("tradeProposition.welcome")
                                              .addStringTemplate("%nation%", settlement.getOwner().getNationName())
                                              .addName("%settlement%", settlement.getName())),
                             Messages.message("tradeProposition.cancel"),
                             choices);
        return (result == null) ? TradeAction.CANCEL : result;
    }

    /**
     * Displays a dialog where the user may choose a file. This is the same as
     * calling:
     *
     * <br>
     * <br>
     * <code>
     * showLoadDialog(directory, new FileFilter[] {FreeColDialog.getFSGFileFilter()});
     * </code>
     *
     * @param directory The directory containing the files.
     * @return The <code>File</code>.
     * @see FreeColDialog
     */
    public File showLoadDialog(File directory) {
        return showLoadDialog(directory, new FileFilter[] { FreeColDialog.getFSGFileFilter() });
    }


    /**
     * Displays a dialog where the user may choose a file.
     *
     * @param directory The directory containing the files.
     * @param fileFilters The file filters which the user can select in the
     *            dialog.
     * @return The <code>File</code>.
     * @see FreeColDialog
     */
    public File showLoadDialog(File directory, FileFilter[] fileFilters) {
        FreeColDialog<File> loadDialog = FreeColDialog.createLoadDialog(freeColClient, gui, directory, fileFilters);

        File response = null;
        showSubPanel(loadDialog);
        for (;;) {
            response = (File) loadDialog.getResponse();
            if (response == null || response.isFile()) break;
            gui.errorMessage("noSuchFile");
        }
        remove(loadDialog);

        return response;
    }

    /**
     * Displays a dialog for setting options when loading a savegame. The
     * settings can be retrieved directly from {@link LoadingSavegameDialog}
     * after calling this method.
     *
     * @param publicServer Default value.
     * @param singleplayer Default value.
     * @return <code>true</code> if the "ok"-button was pressed and
     *         <code>false</code> otherwise.
     */
    public boolean showLoadingSavegameDialog(boolean publicServer, boolean singleplayer) {
        loadingSavegameDialog.initialize(publicServer, singleplayer);
        return showFreeColDialog(loadingSavegameDialog);
    }

    public void showLogFilePanel() {
        showSubPanel(new ErrorPanel(freeColClient, gui));

    }

    public OptionGroup showMapGeneratorOptionsDialog(OptionGroup mgo, boolean editable, boolean loadCustomOptions) {
        return showFreeColDialog(new MapGeneratorOptionsDialog(freeColClient, gui, mgo, editable, loadCustomOptions));
    }

    public Dimension showMapSizeDialog() {
        return showFreeColDialog(FreeColDialog.createMapSizeDialog(freeColClient, gui));

    }

    /**
     * Displays a number of ModelMessages.
     *
     * @param modelMessages
     */
    public void showModelMessages(ModelMessage... modelMessages) {
        List<ModelMessage> messages = filterEventPanels(modelMessages);
        if (messages.size() <= 0) return;
        Game game = freeColClient.getGame();
        String[] messageText = new String[messages.size()];
        ImageIcon[] messageIcon = new ImageIcon[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            messageText[i] = Messages.message(messages.get(i));
            messageIcon[i] = gui.getImageIcon(game
                .getMessageDisplay(messages.get(i)), false);
        }

        // source should be the same for all messages
        FreeColGameObject source = game.getMessageSource(messages.get(0));
        if ((source instanceof Europe && !europePanel.isShowing())
            || (source instanceof Colony || source instanceof WorkLocation)) {
            FreeColDialog<Boolean> confirmDialog
                = FreeColDialog.createConfirmDialog(freeColClient, gui, messageText, messageIcon,
                    Messages.message("ok"), Messages.message("display"));
            if (showFreeColDialog(confirmDialog)) {
                if (!isShowingSubPanel()) {
                    freeColClient.getInGameController().nextModelMessage();
                }
            } else {
                if (source instanceof Europe) {
                    showEuropePanel();
                } else if (source instanceof Colony) {
                    gui.showColonyPanel((Colony) source);
                } else if (source instanceof WorkLocation) {
                    gui.showColonyPanel(((WorkLocation) source).getColony());
                }
            }
        } else {
        	StringBuilder builder = new StringBuilder();
        	for (String s : messageText) {
        		builder.append(s + "\n");
        	}
			final String message = builder.toString();
			SwingUtilities.invokeLaterOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(freeColClient.getActivity(), message,
							Toast.LENGTH_SHORT).show();
				}
			});
//            showSubPanel(new InformationDialog(freeColClient, gui,
//            messageText, messageIcon));
//            if (!isShowingSubPanel()) {
//                freeColClient.getInGameController().nextModelMessage();
//            }
        }
    }

    public boolean showMonarchPanelDialog(MonarchAction action, StringTemplate replace) {
        return showFreeColDialog(new MonarchPanel(freeColClient, gui, action, replace));
    }

    /**
     * Displays the <code>NegotiationDialog</code>.
     *
     * @param unit The <code>Unit</code> that is negotiating.
     * @param settlement A <code>Settlement</code> that is negotiating.
     * @param agreement The current <code>DiplomaticTrade</code> agreement.
     * @return An updated agreement.
     * @see NegotiationDialog
     */
    public DiplomaticTrade showNegotiationDialog(Unit unit, Settlement settlement, DiplomaticTrade agreement) {
        NegotiationDialog negotiationDialog
            = new NegotiationDialog(freeColClient, gui, unit, settlement, agreement);
        negotiationDialog.initialize();
        return showFreeColDialog(negotiationDialog, unit.getTile());
    }

    /**
     * Shows the <code>OpenGamePanel</code>.
     */
    public void showOpenGamePanel() {
        gui.errorMessage("openGame.unimplemented");
    }

    /**
     * Display a dialog to confirm a combat.
     *
     * @param attacker The attacker.
     * @param defender The defender.
     * @param tile A <code>Tile</code> to make visible.
     * @return True if the combat is to proceed.
     */
    public boolean showPreCombatDialog(FreeColGameObject attacker,
                                       FreeColGameObject defender,
                                       Tile tile) {
        return showFreeColDialog(new PreCombatDialog(freeColClient, gui, attacker, defender),
                                 tile);
    }

    public void showReportCargoPanel() {
        showSubPanel(new ReportCargoPanel(freeColClient, gui));

    }

    public void showReportColonyPanel() {
        showSubPanel(new ReportColonyPanel(freeColClient, gui));

    }

    public void showReportContinentalCongressPanel() {
        showSubPanel(new ReportContinentalCongressPanel(freeColClient, gui));
    }

    public void showReportEducationPanel() {
        showSubPanel(new ReportEducationPanel(freeColClient, gui));

    }

    public void showReportExplorationPanel() {
        showSubPanel(new ReportExplorationPanel(freeColClient, gui));
    }

    public void showReportForeignAffairPanel() {
        showSubPanel(new ReportForeignAffairPanel(freeColClient, gui));
    }

    public void showReportHistoryPanel() {
        showSubPanel(new ReportHistoryPanel(freeColClient, gui));
    }

    public void showReportIndianPanel() {
        showSubPanel(new ReportIndianPanel(freeColClient, gui));
    }

    public void showReportLabourPanel() {
        showSubPanel(new ReportLabourPanel(freeColClient, gui));

    }

    public void showReportMilitaryPanel() {
        showSubPanel(new ReportMilitaryPanel(freeColClient, gui));
    }

    public void showReportNavalPanel() {
        showSubPanel(new ReportNavalPanel(freeColClient, gui));

    }

    public void showReportProductionPanel() {
        showSubPanel(new ReportProductionPanel(freeColClient, gui));

    }

    public void showReportReligiousPanel() {
        showSubPanel(new ReportReligiousPanel(freeColClient, gui));

    }

    public void showReportRequirementsPanel() {
        showSubPanel(new ReportRequirementsPanel(freeColClient, gui));
    }

    public void showReportTradePanel() {
        showSubPanel(new ReportTradePanel(freeColClient, gui));

    }

    /**
     * Show the new turn report.
     *
     * @param messages The <code>ModelMessage</code>s to show.
     */
    public void showReportTurnPanel(ModelMessage... messages) {
        showSubPanel(new ReportTurnPanel(freeColClient, gui, messages));
    }


    public int showRiverStyleDialog() {
        return showFreeColDialog(new RiverStylePanel(freeColClient, gui));
    }

    /**
     * Displays a dialog where the user may choose a filename. This is the same
     * as calling:
     *
     * <br>
     * <br>
     * <code>
     * showSaveDialog(directory, new FileFilter[] {FreeColDialog.getFSGFileFilter()}, defaultName);
     * </code>
     *
     * @param directory The directory containing the files in which the user may
     *            overwrite.
     * @param defaultName Default filename for the savegame.
     * @return The <code>File</code>.
     * @see FreeColDialog
     */
    public File showSaveDialog(File directory, String defaultName) {
    	//FIXME Implement save game dialog
    	return new File(directory, defaultName);
//        return showSaveDialog(directory, ".fsg", new FileFilter[] { FreeColDialog.getFSGFileFilter() }, defaultName);
    }

    /**
     * Displays a dialog where the user may choose a filename.
     *
     * @param directory The directory containing the files in which the user may
     *            overwrite.
     * @param standardName This extension will be added to the specified
     *            filename (if not added by the user).
     * @param fileFilters The available file filters in the dialog.
     * @param defaultName Default filename for the savegame.
     * @return The <code>File</code>.
     * @see FreeColDialog
     */
    public File showSaveDialog(File directory, String standardName, FileFilter[] fileFilters, String defaultName) {
        FreeColDialog<File> saveDialog = FreeColDialog.createSaveDialog(freeColClient, gui, directory, standardName, fileFilters, defaultName);
        return showFreeColDialog(saveDialog);
    }


    /**
     * Displays a dialog that asks the user what he wants to do with his scout
     * in the foreign colony.
     *
     * @param colony The <code>Colony</code> to be scouted.
     * @param unit The <code>Unit</code> that is scouting.
     * @param canNegotiate True if negotation is a valid choice.
     * @return The selected action, either negotiate, spy, attack or cancel.
     */
    public ScoutColonyAction showScoutForeignColonyDialog(Colony colony,
                                                          Unit unit,
                                                          boolean canNegotiate) {
        List<ChoiceItem<ScoutColonyAction>> choices
            = new ArrayList<ChoiceItem<ScoutColonyAction>>();
        // We cannot negotiate with the REF
        choices.add(new ChoiceItem<ScoutColonyAction>(
                Messages.message("scoutColony.negotiate"),
                ScoutColonyAction.FOREIGN_COLONY_NEGOTIATE, canNegotiate));
        choices.add(new ChoiceItem<ScoutColonyAction>(
                Messages.message("scoutColony.spy"),
                ScoutColonyAction.FOREIGN_COLONY_SPY));
        choices.add(new ChoiceItem<ScoutColonyAction>(
                Messages.message("scoutColony.attack"),
                ScoutColonyAction.FOREIGN_COLONY_ATTACK));
        StringTemplate template = StringTemplate.template("scoutColony.text")
            .addStringTemplate("%unit%", Messages.getLabel(unit))
            .addName("%colony%", colony.getName());
        ScoutColonyAction result =
            showChoiceDialog(unit.getTile(), Messages.message(template),
        Messages.message("cancel"), choices);
        return (result == null) ? ScoutColonyAction.CANCEL : result;
    }

    /**
     * Displays a dialog that asks the user what he wants to do with
     * his scout in a native settlement.
     *
     * @param settlement The <code>IndianSettlement</code> to be scouted.
     * @param number The number of settlements in the settlement owner nation.
     * @return The chosen action, speak, tribute, attack or cancel.
     */
    public ScoutIndianSettlementAction showScoutIndianSettlementDialog(IndianSettlement settlement, String number) {
        StringBuilder text = new StringBuilder(400);
        Player owner = settlement.getOwner();
        text.append(Messages.message(StringTemplate.template(settlement.getAlarmLevelMessageId(freeColClient.getMyPlayer()))
                                     .addStringTemplate("%nation%", owner.getNationName())));
        text.append("\n\n");
        text.append(Messages.message(StringTemplate.template("scoutSettlement.greetings")
                                     .addStringTemplate("%nation%", settlement.getOwner().getNationName())
                                     .addName("%settlement%", settlement.getName())
                                     .add("%number%", number)
                                     .add("%settlementType%", ((IndianNationType) owner.getNationType()).getSettlementTypeKey(true))));
        text.append(" ");
        if (settlement.getLearnableSkill() != null) {
            text.append(Messages.message(StringTemplate.template("scoutSettlement.skill")
                                         .add("%skill%", settlement.getLearnableSkill().getNameKey())));
            text.append(" ");
        }
        GoodsType[] wantedGoods = settlement.getWantedGoods();
        if (wantedGoods.length > 0) {
            StringTemplate template
                = StringTemplate.template("scoutSettlement.trade."
                    + Integer.toString(wantedGoods.length));
            for (int i = 0; i < wantedGoods.length; i++) {
                if (wantedGoods[i] != null) {
                    template.add("%goods" + Integer.toString(i+1) + "%",
                        wantedGoods[i].getNameKey());
                }
            }
            text.append(Messages.message(template) + "\n\n");
        }

        List<ChoiceItem<ScoutIndianSettlementAction>> choices
            = new ArrayList<ChoiceItem<ScoutIndianSettlementAction>>();
        choices.add(new ChoiceItem<ScoutIndianSettlementAction>(
                Messages.message("scoutSettlement.speak"),
                ScoutIndianSettlementAction.INDIAN_SETTLEMENT_SPEAK));
        choices.add(new ChoiceItem<ScoutIndianSettlementAction>(
                Messages.message("scoutSettlement.tribute"),
                ScoutIndianSettlementAction.INDIAN_SETTLEMENT_TRIBUTE));
        choices.add(new ChoiceItem<ScoutIndianSettlementAction>(
                Messages.message("scoutSettlement.attack"),
                ScoutIndianSettlementAction.INDIAN_SETTLEMENT_ATTACK));
        ScoutIndianSettlementAction result = showChoiceDialog(settlement.getTile(),
                text.toString(),
                Messages.message("cancel"),
                choices);
        return (result == null) ? ScoutIndianSettlementAction.CANCEL : result;
    }

    public int showSelectAmountDialog(GoodsType goodsType, int available, int defaultAmount, boolean needToPay) {
        return showFreeColDialog(new SelectAmountDialog(freeColClient, gui, goodsType, available, defaultAmount, needToPay));
    }

    /**
     * Display a dialog allowing the user to select a destination for
     * a given unit.
     *
     * @param unit The <code>Unit</code> to select a destination for.
     * @return A destination for the unit, or null.
     */
    public Location showSelectDestinationDialog(Unit unit) {
        return showFreeColDialog(new SelectDestinationDialog(freeColClient, gui, unit),
                                 unit.getTile());
    }

    /**
     * Displays the panel for negotiating a sale to a settlement.
     *
     * @param unit The <code>Unit</code> that is selling.
     * @param settlement The <code>Settlement</code> to sell to.
     * @param goods The <code>Goods</code> to sell.
     * @param gold The current negotiated price.
     * @return The chosen action, sell, gift or haggle, or null.
     */
    public SellAction showSellDialog(Unit unit, Settlement settlement,
                                     Goods goods, int gold) {
        StringTemplate goodsTemplate
            = StringTemplate.template("model.goods.goodsAmount")
            .add("%goods%", goods.getType().getNameKey())
            .addAmount("%amount%", goods.getAmount());
        StringTemplate nation = settlement.getOwner().getNationName();
        List<ChoiceItem<SellAction>> choices
            = new ArrayList<ChoiceItem<SellAction>>();
        choices.add(new ChoiceItem<SellAction>(
                Messages.message("sell.takeOffer"),
                SellAction.SELL));
        choices.add(new ChoiceItem<SellAction>(
                Messages.message("sell.moreGold"),
                SellAction.HAGGLE));
        choices.add(new ChoiceItem<SellAction>(
                Messages.message(StringTemplate.template("sell.gift")
                                 .addStringTemplate("%goods%", goodsTemplate)),
                SellAction.GIFT));
        SellAction result = showChoiceDialog(unit.getTile(),
                Messages.message(StringTemplate.template("sell.text")
                                 .addStringTemplate("%nation%", nation)
                                 .addStringTemplate("%goods%", goodsTemplate)
                                 .addAmount("%gold%", gold)),
                Messages.message("sellProposition.cancel"),
                choices);
        return (result == null) ? SellAction.CANCEL : result;
    }

    /**
     * Displays the <code>ServerListPanel</code>.
     *
     * @param username The username that should be used when connecting to one
     *            of the servers on the list.
     * @param serverList The list containing the servers retrieved from the
     *            metaserver.
     * @see ServerListPanel
     */
    public void showServerListPanel(String username, ArrayList<ServerInfo> serverList) {
        serverListPanel.initialize(username, serverList);
        showSubPanel(serverListPanel);
    }

    public void showSettlement(Settlement s) {
        if (s instanceof Colony) {
            if (s.getOwner().equals(freeColClient.getMyPlayer())) {
                gui.showColonyPanel((Colony) s);
            } else if (FreeCol.isInDebugMode()) {
                debugForeignColony(s);
            }
        } else if (s instanceof IndianSettlement) {
            showIndianSettlementPanel((IndianSettlement) s);
        } else {
            throw new IllegalStateException("Bogus settlement");
        }
    }

    /**
     * Displays a dialog with a text and a cancel-button, in addition
     * to buttons for each of the objects in the list.
     *
     * @param tile An optional tile to make visible (not under the dialog).
     * @param text The text that explains the choice for the user.
     * @param cancelText The text displayed on the "cancel"-button.
     * @param objects The List containing the objects to create buttons for.
     * @return The chosen object, or <i>null</i> for the cancel-button.
     */
    public <T> T showSimpleChoiceDialog(Tile tile,
                                        String text, String cancelText,
                                        List<T> objects) {
        List<ChoiceItem<T>> choices = new ArrayList<ChoiceItem<T>>();
        for (T object : objects) {
            choices.add(new ChoiceItem<T>(object));
        }
        return showChoiceDialog(tile,
                                Messages.message(text),
                                Messages.message(cancelText),
                                choices);
    }

    public void showStatisticsPanel() {
        showSubPanel(new StatisticsPanel(freeColClient, gui));
    }

    public void showTilePanel(Tile tile) {
        showSubPanel(new TilePanel(freeColClient, gui, tile));
    }

    /**
     * Shows a tile popup.
     *
     * @param tile The Tile where the popup occurred.
     * @param x The x-coordinate on the screen where the popup needs to be
     *            placed.
     * @param y The y-coordinate on the screen where the popup needs to be
     *            placed.
     * @see TilePopup
     */
    public void showTilePopup(Tile tile, int x, int y) {
        if (tile == null)
            return;
        TilePopup tp = new TilePopup(freeColClient, gui, tile);
        if (tp.hasItem()) {
            tp.show(this, x, y);
            tp.repaint();
        } else if (tile.isExplored()) {
            showTilePanel(tile);
        }
    }

    /**
     * Display a dialog to select a trade route for a unit.
     *
     * @param unit The <code>Unit</code> to select a trade route for.
     * @return A trade route, or null.
     */
    public TradeRoute showTradeRouteDialog(TradeRoute tradeRoute, Tile tile) {
        return showFreeColDialog(new TradeRouteDialog(freeColClient, gui, tradeRoute),
                                 tile);
    }

    public boolean showTradeRouteInputDialog(TradeRoute newRoute) {
        return showFreeColDialog(new TradeRouteInputDialog(freeColClient,  gui, newRoute));
    }

    /**
     * Displays a dialog that asks the user what he wants to do with his
     * missionary in the indian settlement.
     *
     * @param unit The <code>Unit</code> speaking to the settlement.
     * @param settlement The <code>IndianSettlement</code> being visited.
     * @param canEstablish Is establish a valid option.
     * @param canDenounce Is denounce a valid option.
     * @return The chosen action, establish mission, denounce, incite
     *         or cancel.
     */
    public MissionaryAction showUseMissionaryDialog(Unit unit,
                                                    IndianSettlement settlement,
                                                    boolean canEstablish,
                                                    boolean canDenounce) {
        List<ChoiceItem<MissionaryAction>> choices
            = new ArrayList<ChoiceItem<MissionaryAction>>();
        choices.add(new ChoiceItem<MissionaryAction>(
                Messages.message("missionarySettlement.establish"),
                MissionaryAction.ESTABLISH_MISSION, canEstablish));
        choices.add(new ChoiceItem<MissionaryAction>(
                Messages.message("missionarySettlement.heresy"),
                MissionaryAction.DENOUNCE_HERESY, canDenounce));
        choices.add(new ChoiceItem<MissionaryAction>(
                Messages.message("missionarySettlement.incite"),
                MissionaryAction.INCITE_INDIANS));
        String messageId = settlement.getAlarmLevelMessageId(unit.getOwner());
        StringBuilder introText
            = new StringBuilder(Messages.message(StringTemplate.template(messageId)
                                                 .addStringTemplate("%nation%", settlement.getOwner().getNationName())));
        introText.append("\n\n");
        introText.append(Messages.message(StringTemplate.template("missionarySettlement.question")
                                          .addName("%settlement%", settlement.getName())));
        MissionaryAction result = showChoiceDialog(unit.getTile(),
                introText.toString(),
                Messages.message("cancel"),
                choices);
        return (result == null) ? MissionaryAction.CANCEL : result;
    }

    public void showVictoryPanel() {
        showSubPanel(new VictoryPanel(freeColClient, gui));
    }

    public boolean showWarehouseDialog(Colony colony) {
        return showFreeColDialog(new WarehouseDialog(freeColClient, gui,
                colony));
    }

    public void showWorkProductionPanel(Unit unit) {
        showSubPanel(new WorkProductionPanel(freeColClient, gui, unit));
    }

    public void updateGameOptions() {
        startGamePanel.updateGameOptions();
    }

    public void updateMapGeneratorOptions() {
        startGamePanel.updateMapGeneratorOptions();

    }

    /**
     * Updates the sizes of the components on this Canvas.
     */
    public void updateSizes() {
        if (oldSize == null) {
            oldSize = getSize();
        }
        if (oldSize.width != getWidth() || oldSize.height != getHeight()) {
            MapControlsAction mca = (MapControlsAction) freeColClient
                .getActionManager().getFreeColAction(MapControlsAction.id);
            MapControls mc = mca.getMapControls();
            if (mc != null && mc.isShowing()) {
                mc.removeFromComponent(this);
                mc.addToComponent(this);
                mapControls = mc;
            }
            mapViewer.setSize(getSize());
            mapViewer.forceReposition();
            oldSize = getSize();
        }
    }

    void displayChat(String senderNme, String message, boolean privateChat) {
        startGamePanel.displayChat(senderNme, message, privateChat);

    }

    /**
     * Refreshes this Canvas visually.
     */
    void refresh() {
        repaint(0, 0, getWidth(), getHeight());
    }

    /**
     * Adds a component on this Canvas inside a frame. Removes the
     * statuspanel if visible (and <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this ToEuropePanel.
     * @param toolBox Should be set to true if the resulting frame
     *      is used as a toolbox (that is: it should not be counted
     *      as a frame).
     * @param popupPosition a <code>PopupPosition</code> value
     * @return The <code>JInternalFrame</code> that was created and added.
     */
    private JInternalFrame addAsFrame(JComponent comp, boolean toolBox, PopupPosition popupPosition) {
        final int FRAME_EMPTY_SPACE = 60;

        final JInternalFrame f = (toolBox) ? new ToolBoxFrame() : new JInternalFrame();
        if (f.getContentPane() instanceof JComponent) {
            JComponent c = (JComponent) f.getContentPane();
            c.setOpaque(false);
            c.setBorder(null);
        }

        if (comp.getBorder() != null) {
            if (comp.getBorder() instanceof EmptyBorder) {
                f.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            } else {
                f.setBorder(comp.getBorder());
                comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
        } else {
            f.setBorder(null);
        }

        final FrameMotionListener fml = new FrameMotionListener(f);
        comp.addMouseMotionListener(fml);
        comp.addMouseListener(fml);
        if (f.getUI() instanceof BasicInternalFrameUI) {
            BasicInternalFrameUI biu = (BasicInternalFrameUI) f.getUI();
            biu.setNorthPane(null);
            biu.setSouthPane(null);
            biu.setWestPane(null);
            biu.setEastPane(null);
        }

        f.getContentPane().add(comp);
        f.setOpaque(false);
        f.pack();
        int width = f.getWidth();
        int height = f.getHeight();
        if (width > getWidth() - FRAME_EMPTY_SPACE) {
            width = Math.min(width, getWidth());
        }
        if (height > getHeight() - FRAME_EMPTY_SPACE) {
            height = Math.min(height, getHeight());
        }
        f.setSize(width, height);
        Point p = null;
        if (comp instanceof FreeColPanel
            && freeColClient.getClientOptions().getBoolean("model.option.rememberPanelPositions")
            && (p = ((FreeColPanel) comp).getSavedPosition()) != null) {
            // Sanity check stuff coming out of client options.
            if (p.getX() < 0
                || p.getX() >= getWidth() - f.getWidth()
                || p.getY() < 0
                || p.getY() >= getHeight() - f.getHeight()) {
                p = null;
            }
        }
        if (p == null) {
            switch (popupPosition) {
            case CENTERED:
                f.setLocation((getWidth() - f.getWidth()) / 2,
                              (getHeight() - f.getHeight()) / 2);
                break;
            case CENTERED_LEFT:
                f.setLocation((getWidth() - f.getWidth()) / 4,
                              (getHeight() - f.getHeight()) / 2);
                break;
            case CENTERED_RIGHT:
                f.setLocation(((getWidth() - f.getWidth()) * 3) / 4,
                              (getHeight() - f.getHeight()) / 2);
                break;
            case ORIGIN:
            default:
                f.setLocation(0, 0);
                break;
            }
        } else {
            f.setLocation(p);
        }
        add(f, MODAL_LAYER);
        f.setName(comp.getClass().getSimpleName());

        f.setFrameIcon(null);
        f.setVisible(true);
        f.setResizable(true);
        try {
            f.setSelected(true);
        } catch (Exception e) {
        }

        return f;
    }

    /**
     * Adds a component centered on this Canvas. Removes the statuspanel if
     * visible (and <code>comp != statusPanel</code>).
     *
     * @param comp The component to add to this ToEuropePanel.
     * @param i The layer to add the component to (see JLayeredPane).
     */
    private void addCentered(Component comp, Integer i) {
        comp.setLocation((getWidth() - comp.getWidth()) / 2,
                         (getHeight() - comp.getHeight()) / 2);

        add(comp, i);
    }

    /**
     * Create key bindings for all actions.
     */
    private void createKeyBindings() {
        for (Option option : freeColClient.getActionManager().getOptions()) {
            FreeColAction action = (FreeColAction) option;
            getInputMap().put(action.getAccelerator(), action.getId());
            getActionMap().put(action.getId(), action);
        }
    }

    /**
     * Filters out and displays the EventPanel messages.
     *
     * @param messages The list of <code>ModelMessage</code> to filter.
     * @return The list of messages without any EventPanel messages.
     */
    private List<ModelMessage> filterEventPanels(ModelMessage[] messages) {
        final String eventMatch = "EventPanel.";
        List<ModelMessage> normal = new ArrayList<ModelMessage>();
        for (int i = 0; i < messages.length; i++) {
            String id = messages[i].getId();
            if (id.startsWith(eventMatch)) {
                id = id.substring(eventMatch.length());
                final EventType e = EventType.valueOf(id);
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showEventPanel(e);
                        }
                    });
            } else {
                normal.add(messages[i]);
            }
        }
        return normal;
    }

    /**
     * Gets any currently displayed colony panel for the specified colony.
     *
     * @param colony The <code>Colony</code> to check.
     * @return A currently displayed colony panel, or null if not found.
     */
    private ColonyPanel getColonyPanel(Colony colony) {
        for (Component c1 : getComponents()) {
            if (c1 instanceof JInternalFrame) {
                for (Component c2 : ((JInternalFrame) c1).getContentPane().getComponents()) {
                    if (c2 instanceof ColonyPanel
                        && ((ColonyPanel) c2).getColony() == colony) {
                        return (ColonyPanel) c2;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Gets the internal frame for the given component.
     *
     * @param c The component.
     * @return The given component if this is an internal frame or the first
     *         parent that is an internal frame. Returns <code>null</code> if
     *         no internal frame is found.
     */
    private JInternalFrame getInternalFrame(final Component c) {
        Component temp = c;

        while (temp != null && !(temp instanceof JInternalFrame)) {
            temp = temp.getParent();
        }
        return (JInternalFrame) temp;
    }

    /**
     * Given a tile to be made visible, determine a position to popup
     * a panel.
     *
     * @param tile A <code>Tile</code> to be made visible.
     * @return A <code>PopupPosition</code> for a panel to be displayed.
     */
    private PopupPosition getPopupPosition(Tile tile) {
        if (tile == null)
            return PopupPosition.CENTERED;
        int where = mapViewer.setOffsetFocus(tile);
        return (where > 0) ? PopupPosition.CENTERED_LEFT
            : (where < 0) ? PopupPosition.CENTERED_RIGHT
            : PopupPosition.CENTERED;
    }

    /**
     * Displays the given dialog, making sure a tile is visible.
     *
     * @param freeColDialog The dialog to be displayed
     * @param tile A <code>Tile</code> to make visible (not under the dialog!)
     * @return The {@link FreeColDialog#getResponse reponse} returned by
     *         the dialog.
     */
    private <T> T showFreeColDialog(FreeColDialog<T> freeColDialog, Tile tile) {
        showFreeColPanel(freeColDialog, tile);
        T response = freeColDialog.getResponse();
        remove(freeColDialog);
        return response;
    }

    /**
     * Displays the given panel, making sure a tile is visible.
     *
     * @param panel The panel to be displayed
     * @param tile A <code>Tile</code> to make visible (not under the panel!)
     */
    private void showFreeColPanel(FreeColPanel panel, Tile tile) {
        showSubPanel(panel, getPopupPosition(tile));
    }


    /**
     * Displays a <code>FreeColPanel</code>.
     * @param panel <code>FreeColPanel</code>, panel to show
     */
    private void showSubPanel(FreeColPanel panel) {
        showSubPanel(panel, PopupPosition.CENTERED);
    }

    /**
     * Displays a <code>FreeColPanel</code> at a generalized position.
     *
     * @param panel <code>FreeColPanel</code>, panel to show
     * @param popupPosition <code>PopupPosition</code> The generalized
     *     position to place the panel.
     */
    private void showSubPanel(FreeColPanel panel, PopupPosition popupPosition) {
        repaint();
        addAsFrame(panel, false, popupPosition);
        panel.requestFocus();
    }

}
