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


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.JTree;
import org.freecolandroid.repackaged.javax.swing.event.HyperlinkEvent;
import org.freecolandroid.repackaged.javax.swing.event.HyperlinkListener;
import org.freecolandroid.repackaged.javax.swing.event.TreeSelectionEvent;
import org.freecolandroid.repackaged.javax.swing.event.TreeSelectionListener;
import org.freecolandroid.repackaged.javax.swing.tree.DefaultMutableTreeNode;
import org.freecolandroid.repackaged.javax.swing.tree.DefaultTreeModel;
import org.freecolandroid.repackaged.javax.swing.tree.TreePath;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;


/**
 * This panel displays the Colopedia.
 */
public final class ColopediaPanel extends FreeColPanel
    implements HyperlinkListener, TreeSelectionListener {

    private static final Logger logger = Logger.getLogger(ColopediaPanel.class.getName());

    private JLabel header;

    private JPanel listPanel;

    private JPanel detailPanel;

    private JTree tree;

    private Map<String, DefaultMutableTreeNode> nodeMap =
        new HashMap<String, DefaultMutableTreeNode>();


    /**
     * The constructor that will add the items to this panel.
     *
     * @param parent The parent of this panel.
     * @param id a <code>String</code> value
     */
    public ColopediaPanel(FreeColClient freeColClient, GUI gui, String id) {
        super(freeColClient, gui);

        setLayout(new MigLayout("fill", "[200:]unrelated[550:, grow, fill]", "[][grow, fill][]"));

        header = getDefaultHeader(Messages.message("menuBar.colopedia"));
        add(header, "span, align center");

        listPanel = new JPanel() {
                @Override
                public String getUIClassID() {
                    return "ColopediaPanelUI";
                }
            };
        listPanel.setOpaque(true);
        JScrollPane sl = new JScrollPane(listPanel,
                                         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sl.getVerticalScrollBar().setUnitIncrement(16);
        sl.getViewport().setOpaque(false);
        add(sl);

        detailPanel = new JPanel() {
                @Override
                public String getUIClassID() {
                    return "ColopediaPanelUI";
                }
            };
        detailPanel.setOpaque(true);
        JScrollPane detail = new JScrollPane(detailPanel,
                                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detail.getVerticalScrollBar().setUnitIncrement(16);
        detail.getViewport().setOpaque(false);
        add(detail, "grow");

        add(okButton, "newline 20, span, tag ok");

        restoreSavedSize(850, 600);
        tree = buildTree();

        select(id);

    }

    /**
     * Creates a new <code>ColopediaPanel</code> instance suitable
     * only for the construction of ColopediaDetailPanels. TODO: find
     * a more elegant solution.
     *
     * @param canvas a <code>Canvas</code> value
     * @see ChooseFoundingFatherDialog
     */
    ColopediaPanel(FreeColClient freeColClient, GUI gui) {
        super(freeColClient, gui);
    }

    /**
     * Builds the JTree which represents the navigation menu and then returns it
     *
     * @return The navigation tree.
     */
    private JTree buildTree() {
        String name = Messages.message("menuBar.colopedia");
        DefaultMutableTreeNode root
            = new DefaultMutableTreeNode(new ColopediaTreeItem(null, null, name, null));

        new TerrainDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new ResourcesDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new GoodsDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new UnitDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new BuildingDetailPanel(getFreeColClient(),  getGUI(), this).addSubTrees(root);
        new FatherDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new NationDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new NationTypeDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);
        new ConceptDetailPanel(getFreeColClient(), getGUI(), this).addSubTrees(root);

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(200, super.getPreferredSize().height);
                }
            };
        tree.setRootVisible(false);
        tree.setCellRenderer(new ColopediaTreeCellRenderer());
        tree.setOpaque(false);
        tree.addTreeSelectionListener(this);

        listPanel.add(tree);
        Enumeration allNodes = root.depthFirstEnumeration();
        while (allNodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.nextElement();
            ColopediaTreeItem item = (ColopediaTreeItem) node.getUserObject();
            nodeMap.put(item.getId(), node);
        }
        return tree;
    }

    /**
     * This function analyses a tree selection event and calls the right methods to take care
     * of building the requested unit's details.
     *
     * @param event The incoming TreeSelectionEvent.
     */
    public void valueChanged(TreeSelectionEvent event) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            showDetails((ColopediaTreeItem) node.getUserObject());
        }
    }

    private void showDetails(ColopediaTreeItem nodeItem) {
        detailPanel.removeAll();
        if (nodeItem.getPanelType() != null && nodeItem.getId() != null) {
            nodeItem.getPanelType().buildDetail(nodeItem.getId(), detailPanel);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void select(String id) {
        DefaultMutableTreeNode node = nodeMap.get(id);
        if (node == null) {
            logger.warning("Unable to find node with id '" + id + "'.");
        } else {
            TreePath oldPath = tree.getSelectionPath();
            if (oldPath != null && oldPath.getParentPath() != null) {
                tree.collapsePath(oldPath.getParentPath());
            }
            TreePath newPath = new TreePath(node.getPath());
            tree.scrollPathToVisible(newPath);
            tree.expandPath(newPath);
            showDetails((ColopediaTreeItem) node.getUserObject());
        }
    }

    /**
     * This function analyses an event and calls the right methods to take care
     * of the user's requests.
     *
     * @param event The incoming ActionEvent.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (OK.equals(command)) {
            getGUI().removeFromCanvas(this);
        } else {
            select(command);
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        HyperlinkEvent.EventType type = e.getEventType();
        if (type == HyperlinkEvent.EventType.ACTIVATED) {
            String[] path = e.getURL().getPath().split("/");
            if ("id".equals(path[1])) {
                select(path[2]);
            } else if ("action".equals(path[1])) {
                getFreeColClient().getActionManager().getFreeColAction(path[2])
                    .actionPerformed(null);
            }
        }
    }

}
