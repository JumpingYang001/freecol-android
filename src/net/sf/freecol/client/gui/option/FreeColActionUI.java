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


package net.sf.freecol.client.gui.option;

import org.freecolandroid.repackaged.java.awt.Color;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.Graphics2D;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.java.awt.event.KeyEvent;
import org.freecolandroid.repackaged.java.awt.event.KeyListener;
import org.freecolandroid.repackaged.java.awt.event.MouseEvent;
import org.freecolandroid.repackaged.java.awt.event.MouseListener;
import org.freecolandroid.repackaged.java.awt.image.BufferedImage;
import org.freecolandroid.repackaged.javax.swing.BorderFactory;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.KeyStroke;
import org.freecolandroid.repackaged.javax.swing.Timer;



import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.action.FreeColAction;


/**
* User interface for displaying/changing a keyboard accelerator for a <code>FreeColAction</code>.
*/
public final class FreeColActionUI extends OptionUI<FreeColAction> implements ActionListener {


    private OptionGroupUI optionGroupUI;
    private KeyStroke keyStroke;
    private JButton recordButton;
    private JButton removeButton;
    private BlinkingLabel bl;
    private JPanel panel = new JPanel();


    /**
     * Creates a new <code>FreeColActionUI</code> for the
     * given <code>FreeColAction</code>.
     *
     * @param option The <code>FreeColAction</code> to make a user
     *       interface for.
     * @param editable boolean whether user can modify the setting
     */
    public FreeColActionUI(GUI gui, FreeColAction option, boolean editable) {
        super(gui, option, editable);

        this.optionGroupUI = optionGroupUI;
        keyStroke = option.getAccelerator();

        panel.add(getLabel());

        bl = new BlinkingLabel();
        panel.add(bl);

        recordButton = new JButton(getRecordImage());
        recordButton.addActionListener(this);
        panel.add(recordButton);

        removeButton = new JButton(getRemoveImage());
        removeButton.addActionListener(this);
        panel.add(removeButton);

        initialize();
    }

    /**
    * Creates an icon for symbolizing the recording of a <code>KeyStroke</code>.
    * @return The <code>ImageIcon</code>.
    */
    public static ImageIcon getRecordImage() {
        BufferedImage bi = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(Color.RED);
        g.fillOval(0, 0, 9, 9);
        g.setColor(Color.BLACK);
        g.drawOval(0, 0, 9, 9);

        return new ImageIcon(bi);
    }


    /**
    * Creates an icon to be used on the button that removes a keyboard accelerator.
    * @return The <code>ImageIcon</code>.
    */
    public static ImageIcon getRemoveImage() {
        BufferedImage bi = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        /*g.fillRect(0, 0, 9, 9);*/
        g.setColor(Color.BLACK);
        g.drawLine(1, 0, 8, 7);
        g.drawLine(0, 1, 7, 8);
        g.drawLine(7, 0, 0, 7);
        g.drawLine(9, 0, 0, 9);
        g.setColor(Color.RED);
        g.drawLine(0, 0, 8, 8);
        g.drawLine(8, 0, 0, 8);

        return new ImageIcon(bi);
    }

    /**
    * Gets a string to represent the given <code>KeyStroke</code> to the user.
    *
    * @param keyStroke <code>java.awt.event.KeyStroke</code>
    * @return String
    */
    public static String getHumanKeyStrokeText(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return " ";
        }

        String s = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
        if (!s.equals("")) {
            s += "+";
        }
        return s + KeyEvent.getKeyText(keyStroke.getKeyCode());
    }


    /**
    * Removes the given <code>KeyStroke</code>. That is:
    * This action's <code>KeyStroke</code> is set to
    * <code>null</code> if it is the same as the given
    * <code>KeyStroke</code>.
    *
    * @param k The <code>KeyStroke</code> to be removed.
    */
    public void removeKeyStroke(KeyStroke k) {
        if (k != null && keyStroke != null
            && k.getKeyCode() == keyStroke.getKeyCode()
            && k.getModifiers() == keyStroke.getModifiers()) {
            keyStroke = null;
            bl.setText(" ");
        }
    }

    public void setOptionGroupUI(OptionGroupUI ui) {
        this.optionGroupUI = ui;
    }


    /**
     * {@inheritDoc}
     */
    public JPanel getComponent() {
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    public void updateOption() {
        getOption().setAccelerator(keyStroke);
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        keyStroke = getOption().getAccelerator();
        bl.setText(getHumanKeyStrokeText(keyStroke));
    }


    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == recordButton) {
            bl.startBlinking();
            bl.requestFocus();
        } else if (evt.getSource() == removeButton) {
            bl.stopBlinking();
            bl.setText(" ");
            keyStroke = null;
        }
    }



    /**
    * Label for displaying a <code>KeyStroke</code>.
    */
    class BlinkingLabel extends JLabel implements ActionListener, KeyListener, MouseListener {

//        private Timer blinkingTimer = new Timer(500, this, gui.get);
        private boolean blinkOn = false;

        BlinkingLabel() {
            super(getHumanKeyStrokeText(keyStroke), JLabel.CENTER);

            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            addKeyListener(this);
            addMouseListener(this);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() > 1) {
                startBlinking();
                requestFocus();
            }
        }


        public void mouseEntered(MouseEvent e) { /* No such event */ }
        public void mouseExited(MouseEvent e) { /* No such event */ }
        public void mousePressed(MouseEvent e) { /* No such event */ }
        public void mouseReleased(MouseEvent e) { /* No such event */ }


        @Override
        public Dimension getMinimumSize() {
            return new Dimension(80, super.getMinimumSize().height);
        }

        @Override
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }


        public void startBlinking() {
//            blinkingTimer.start();
        }


        public void stopBlinking() {
//            blinkingTimer.stop();
            setOpaque(false);
            repaint();
        }

        public void actionPerformed(ActionEvent evt) {
            if (!hasFocus()) {
                stopBlinking();
            }

            if (blinkOn) {
                setOpaque(false);
                blinkOn = false;
                repaint();
            } else {
                setOpaque(true);
                setBackground(Color.RED);
                blinkOn = true;
                repaint();
            }
        }


        public void keyPressed(KeyEvent e) { /* No such event */ }

        public void keyTyped(KeyEvent e) { /* No such event */ }

        public void keyReleased(KeyEvent e) {
            KeyStroke ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());
            optionGroupUI.removeKeyStroke(ks);
            keyStroke = ks;
            //keyStroke = KeyStroke.getKeyStroke(new Character(e.getKeyChar()), e.getModifiers());
            stopBlinking();
            setText(getHumanKeyStrokeText(keyStroke));
            recordButton.requestFocus();
        }
    }
}
