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


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.freecolandroid.repackaged.java.awt.AWTEvent;
import org.freecolandroid.repackaged.java.awt.ActiveEvent;
import org.freecolandroid.repackaged.java.awt.BorderLayout;
import org.freecolandroid.repackaged.java.awt.Component;
import org.freecolandroid.repackaged.java.awt.Dimension;
import org.freecolandroid.repackaged.java.awt.EventQueue;
import org.freecolandroid.repackaged.java.awt.GridLayout;
import org.freecolandroid.repackaged.java.awt.MenuComponent;
import org.freecolandroid.repackaged.java.awt.event.ActionEvent;
import org.freecolandroid.repackaged.java.awt.event.ActionListener;
import org.freecolandroid.repackaged.javax.swing.ImageIcon;
import org.freecolandroid.repackaged.javax.swing.JButton;
import org.freecolandroid.repackaged.javax.swing.JFileChooser;
import org.freecolandroid.repackaged.javax.swing.JLabel;
import org.freecolandroid.repackaged.javax.swing.JPanel;
import org.freecolandroid.repackaged.javax.swing.JScrollPane;
import org.freecolandroid.repackaged.javax.swing.JTextArea;
import org.freecolandroid.repackaged.javax.swing.JTextField;
import org.freecolandroid.repackaged.javax.swing.SwingUtilities;
import org.freecolandroid.repackaged.javax.swing.border.CompoundBorder;
import org.freecolandroid.repackaged.javax.swing.border.EmptyBorder;
import org.freecolandroid.repackaged.javax.swing.filechooser.FileFilter;


import net.miginfocom.swing.MigLayout;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;


/**
 * Superclass for all dialogs in FreeCol. This class also contains
 * methods to create simple dialogs.
 */
public class FreeColDialog<T> extends FreeColPanel {

    static final class FreeColFileFilter extends FileFilter {

        private final String  extension1;
        private final String  extension2;
        private final String  description;

        FreeColFileFilter( String  extension, String  descriptionMessage ) {

            this.extension1 = extension;
            this.extension2 = "....";
            description = Messages.message(descriptionMessage);
        }

        FreeColFileFilter( String  extension1,
                           String  extension2,
                           String  descriptionMessage ) {

            this.extension1 = extension1;
            this.extension2 = extension2;
            description = Messages.message(descriptionMessage);
        }

        public boolean accept(File f) {

            return f.isDirectory() || f.getName().endsWith(extension1)
                || f.getName().endsWith(extension2);
        }

        public String getDescription() {

            return description;
        }
    }

    private static final Logger logger = Logger.getLogger(FreeColDialog.class.getName());

    protected static final String CANCEL = "CANCEL";

    /**
     * Creates a new <code>FreeColDialog</code> with a text and a
     * cancel-button, in addition to buttons for each of the objects
     * in the given array.
     *
     * @param text String, text that explains the choice for the user
     * @param cancelText String, text displayed on the "cancel"-button
     * @param choices List of <code>ChoiceItem<T> <code> the choice items
     * @return <code>FreeColDialog</code>
     * @see ChoiceItem
     */
    public static <T> FreeColDialog<ChoiceItem<T>> createChoiceDialog(FreeColClient freeColClient, GUI gui, String text,
        String cancelText, List<ChoiceItem<T>> choices) {

        if (choices.isEmpty()) {
            throw new IllegalArgumentException("Can not create choice dialog with 0 choices!");
        }

        final List<JButton> choiceBtnLst = new ArrayList<JButton>();
        final FreeColDialog<ChoiceItem<T>> choiceDialog
            = new FreeColDialog<ChoiceItem<T>>(freeColClient, gui) {
                @Override
                public void requestFocus() {
                    for (JButton b : choiceBtnLst) {
                        if (b.isEnabled()) {
                            b.requestFocus();
                            return;
                        }
                    }
                }
            };

        choiceDialog.setLayout(new MigLayout("fillx, wrap 1", "[align center]", ""));
        JTextArea textArea = getDefaultTextArea(text);

        choiceDialog.add(textArea);

        int columns = 1;
             if ((choices.size() % 4) == 0 && choices.size() > 12) columns = 4;
        else if ((choices.size() % 3) == 0 && choices.size() > 6)  columns = 3;
        else if ((choices.size() % 2) == 0 && choices.size() > 4)  columns = 2;

        else if (choices.size() > 21) columns = 4;
        else if (choices.size() > 10) columns = 2;

        JPanel choicesPanel = new JPanel(new GridLayout(0, columns, 10, 10));
        choicesPanel.setBorder(new CompoundBorder(choicesPanel.getBorder(),
                new EmptyBorder(10, 20, 10, 20)));

        /*
        final ChoiceItem<T> firstObject = choices.get(0);
        if(firstObject.isEnabled()){
            firstButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    choiceDialog.setResponse(firstObject);
                }
            });
        }
        firstButton.setEnabled(firstObject.isEnabled());
        choicesPanel.add(firstButton);
        choices.remove(0);
        */

        for (final ChoiceItem<T> object : choices) {
            final JButton objectButton = new JButton(object.toString());
            if (object.isEnabled()) {
                objectButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        choiceDialog.setResponse(object);
                    }
                });
                enterPressesWhenFocused(objectButton);
            }
            objectButton.setEnabled(object.isEnabled());
            choiceBtnLst.add(objectButton);
            choicesPanel.add(objectButton);
        }
        if (choices.size() > 20) {
            JScrollPane scrollPane = new JScrollPane(choicesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            choiceDialog.add(scrollPane, "newline 20");
        } else {
            choicesPanel.setOpaque(false);
            choiceDialog.add(choicesPanel, "newline 20");
        }

        if (cancelText != null) {
            choiceDialog.cancelButton.setText(cancelText);
            choiceDialog.add(choiceDialog.cancelButton, "newline 20, tag cancel");
            choiceDialog.cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        choiceDialog.setResponse(null);
                    }
                });
        }

        choiceDialog.setSize(choiceDialog.getPreferredSize());

        return choiceDialog;
    }

    /**
     * Creates a new <code>FreeColDialog</code> with a text and a
     * ok/cancel option.  The "ok"-option calls {@link #setResponse
     * setResponse(new Boolean(true))} and the "cancel"-option calls
     * {@link #setResponse setResponse(new Boolean(false))}.
     *
     * @param text The text that explains the choice for the user.
     * @param okText The text displayed on the "ok"-button.
     * @param cancelText The text displayed on the "cancel"-button.
     * @return The <code>FreeColDialog</code>.
     */
    public static FreeColDialog<Boolean> createConfirmDialog(final FreeColClient freeColClient, GUI gui, String text, String okText, String cancelText) {
        return createConfirmDialog(freeColClient, gui, new String[] {text}, null, okText, cancelText);
    }


    public static FreeColDialog<Boolean> createConfirmDialog(FreeColClient freeColClient, GUI gui, String[] texts,
        ImageIcon[] icons, String okText, String cancelText) {
        // create the dialog
        final FreeColDialog<Boolean> confirmDialog
            = new FreeColDialog<Boolean>(freeColClient, gui);

        confirmDialog.setLayout(new MigLayout("wrap 2", "[][fill]", ""));

        confirmDialog.okButton.setText(okText);
        confirmDialog.okButton.addActionListener(new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    confirmDialog.setResponse(Boolean.TRUE);
                }
            });

        confirmDialog.cancelButton.setText(cancelText);
        confirmDialog.cancelButton.removeActionListener(confirmDialog);
        confirmDialog.cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    confirmDialog.setResponse(Boolean.FALSE);
                }
            });

        for (int i = 0; i < texts.length; i++) {
            if (icons != null && icons[i] != null) {
                confirmDialog.add(new JLabel(icons[i]));
                confirmDialog.add(getDefaultTextArea(texts[i]));
            } else {
                confirmDialog.add(getDefaultTextArea(texts[i]), "skip");
            }
        }

        confirmDialog.add(confirmDialog.okButton, "newline 20, span, split 2, tag ok");
        confirmDialog.add(confirmDialog.cancelButton, "tag cancel");

        return confirmDialog;
    }

    /**
     * Creates a new <code>FreeColDialog</code> with a text field and a
     * ok/cancel option.  The "ok"-option calls {@link #setResponse
     * setResponse(textField.getText())} and the "cancel"-option calls
     * {@link #setResponse setResponse(null)}.
     *
     * @param text The text that explains the action to the user.
     * @param defaultValue The default value appearing in the text field.
     * @param okText The text displayed on the "ok"-button.
     * @param cancelText The text displayed on the "cancel"-button.
     * @return The <code>FreeColDialog</code>.
     */
    public static FreeColDialog<String> createInputDialog(FreeColClient freeColClient, GUI gui, String text,
        String defaultValue, String okText, String cancelText) {

        final JTextField input = new JTextField(defaultValue);
        final FreeColDialog<String> inputDialog
            = new FreeColDialog<String>(freeColClient, gui)  {
                @Override
                public void requestFocus() {
                    input.requestFocus();
                }
            };

        inputDialog.setLayout(new MigLayout("wrap 1, gapy 20", "", ""));

        inputDialog.okButton.setText(okText);
        inputDialog.okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                inputDialog.setResponse(input.getText());
            }
        });

        input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                inputDialog.setResponse(input.getText());
            }
        });

        input.selectAll();

        inputDialog.add(getDefaultTextArea(text));
        inputDialog.add(input, "width 180:, growx");

        if (cancelText == null) {
            inputDialog.add(inputDialog.okButton, "tag ok");
            inputDialog.setCancelComponent(inputDialog.okButton);
            inputDialog.okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        inputDialog.setResponse(null);
                    }
                });
        } else {
            inputDialog.cancelButton.setText(cancelText);
            inputDialog.add(inputDialog.okButton, "split 2, tag ok");
            inputDialog.add(inputDialog.cancelButton, "tag cancel");
            inputDialog.cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        inputDialog.setResponse(null);
                    }
                });
        }

        inputDialog.setSize(inputDialog.getPreferredSize());

        return inputDialog;
    }

    /**
     * Creates a new <code>FreeColDialog</code> in which the user
     * may choose a savegame to load.
     *
     * @param directory The directory to display when choosing the file.
     * @param fileFilters The available file filters in the
     *       dialog.
     * @return The <code>FreeColDialog</code>.
     */
    public static FreeColDialog<File> createLoadDialog(FreeColClient freeColClient, GUI gui, File directory,
                                                       FileFilter[] fileFilters) {
        final FreeColDialog<File> loadDialog
            = new FreeColDialog<File>(freeColClient, gui);
        final JFileChooser fileChooser = new JFileChooser(directory);

        loadDialog.okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        loadDialog.setResponse(selectedFile);
                    }
                }
            });

        if (fileFilters.length > 0) {
            for (FileFilter fileFilter : fileFilters) {
                fileChooser.addChoosableFileFilter(fileFilter);
            }
            fileChooser.setFileFilter(fileFilters[0]);
            fileChooser.setAcceptAllFileFilterUsed(false);
        }
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setControlButtonsAreShown(false);
        loadDialog.setLayout(new MigLayout("fill", "", ""));
        loadDialog.add(fileChooser, "grow");
        loadDialog.add(loadDialog.okButton, "newline 20, split 2, tag ok");
        loadDialog.add(loadDialog.cancelButton, "tag cancel");
        loadDialog.cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    loadDialog.setResponse(null);
                }
            });
        loadDialog.setSize(480, 320);

        return loadDialog;
    }

    public static FreeColDialog<Dimension> createMapSizeDialog(FreeColClient freeColClient, final GUI gui) {

        final int defaultHeight = 100;
        final int defaultWidth = 40;
        final int COLUMNS = 5;

        final String widthText = Messages.message("width");
        final String heightText = Messages.message("height");

        final JTextField inputWidth = new JTextField(Integer.toString(defaultWidth), COLUMNS);
        final JTextField inputHeight = new JTextField(Integer.toString(defaultHeight), COLUMNS);

        final FreeColDialog<Dimension> mapSizeDialog = new FreeColDialog<Dimension>(freeColClient, gui);

        mapSizeDialog.setLayout(new MigLayout("wrap 2"));

        mapSizeDialog.okButton.setText(Messages.message("ok"));
        mapSizeDialog.okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        int width = Integer.parseInt(inputWidth.getText());
                        int height = Integer.parseInt(inputHeight.getText());
                        if (width <= 0 || height <= 0) {
                            throw new NumberFormatException();
                        }
                        mapSizeDialog.setResponse(new Dimension(width, height));
                    } catch (NumberFormatException nfe) {
                        gui.errorMessage("integerAboveZero");
                    }
                }
            });


        JLabel widthLabel = new JLabel(widthText);
        widthLabel.setLabelFor(inputWidth);
        JLabel heightLabel = new JLabel(heightText);
        heightLabel.setLabelFor(inputHeight);

        mapSizeDialog.add(new JLabel(Messages.message("editor.mapSize")), "span, align center");
        mapSizeDialog.add(widthLabel, "newline 20");
        mapSizeDialog.add(inputWidth);
        mapSizeDialog.add(heightLabel);
        mapSizeDialog.add(inputHeight);

        mapSizeDialog.add(mapSizeDialog.okButton, "newline 20, span, split2, tag ok");
        mapSizeDialog.add(mapSizeDialog.cancelButton, "tag cancel");
        mapSizeDialog.cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    mapSizeDialog.setResponse(null);
                }
            });

        mapSizeDialog.setSize(mapSizeDialog.getPreferredSize());

        return mapSizeDialog;
    }

    /**
     * Creates a new <code>FreeColDialog</code> in which the user
     * may choose the destination of the savegame.
     *
     * @param directory The directory to display when choosing the name.
     * @param standardName This extension will be added to the
     *       specified filename (if not added by the user).
     * @param fileFilters The available file filters in the
     *       dialog.
     * @param defaultName Default filename for the savegame.
     * @return The <code>FreeColDialog</code>.
     */
    public static FreeColDialog<File> createSaveDialog(FreeColClient freeColClient, GUI gui, File directory,
        final String standardName, FileFilter[] fileFilters, String defaultName) {
        final FreeColDialog<File> saveDialog
            = new FreeColDialog<File>(freeColClient, gui);
        final JFileChooser fileChooser = new JFileChooser(directory);
        final File defaultFile = new File(defaultName);

        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fileFilters.length > 0) {
            for (int i=0; i<fileFilters.length; i++) {
                fileChooser.addChoosableFileFilter(fileFilters[i]);
            }
            fileChooser.setFileFilter(fileFilters[0]);
            fileChooser.setAcceptAllFileFilterUsed(false);
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String actionCommand = event.getActionCommand();
                if (actionCommand.equals(JFileChooser.APPROVE_SELECTION)) {
                    File file = fileChooser.getSelectedFile();
                    if (standardName != null && !file.getName().endsWith(standardName)) {
                        file = new File(file.getAbsolutePath() + standardName);
                    }
                    saveDialog.setResponse(file);
                }
                else if (actionCommand.equals(JFileChooser.CANCEL_SELECTION)) {
                    saveDialog.setResponse(null);
                }
            }
        });
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setSelectedFile(defaultFile);
        saveDialog.setLayout(new BorderLayout());
        saveDialog.add(fileChooser);
        saveDialog.setSize(480, 320);

        return saveDialog;
    }


    /**
     * Returns a filter accepting "*.fgo".
     * @return The filter.
     */
    public static FileFilter getFGOFileFilter() {
        return new FreeColFileFilter( ".fgo", "filter.gameOptions" );
    }

    /**
     * Returns a filter accepting "*.fsg".
     * @return The filter.
     */
    public static FileFilter getFSGFileFilter() {
        return new FreeColFileFilter( ".fsg", "filter.savedGames" );
    }

    /**
     * Returns a filter accepting all files containing a
     * {@link net.sf.freecol.common.model.GameOptions}.
     * That is; both "*.fgo" and "*.fsg".
     *
     * @return The filter.
     */
    public static FileFilter getGameOptionsFileFilter() {
        return new FreeColFileFilter( ".fgo", ".fsg", "filter.gameOptionsAndSavedGames" );
    }


    // Stores the response from the user:
    private T response = null;

    // Whether or not the user have made the choice.
    private boolean responseGiven = false;


    protected JButton cancelButton = new JButton(Messages.message("cancel"));

    /**
     * Constructor.
     *
     * @param parent The parent <code>Canvas</code>.
     */
    public FreeColDialog(FreeColClient freeColClient, GUI gui) {
        super(freeColClient, gui);

        cancelButton.setActionCommand(CANCEL);
        cancelButton.addActionListener(this);
        enterPressesWhenFocused(cancelButton);
        setCancelComponent(cancelButton);
    }

    /**
     * This function analyses an event and calls the right methods to
     * take care of the user's requests.
     *
     * @param event The incoming ActionEvent.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (CANCEL.equals(command)) {
            setResponse(null);
        } else {
            super.actionPerformed(event);
        }
    }

    /**
     * Returns the <code>response</code> when set by
     * <code>setResponse(Object response)</code>.
     * Waits the thread until then.
     *
     * @return The object as set by {@link #setResponse}.
     */
    public synchronized T getResponse() {
        // Wait the thread until 'response' is available. Notice that
        // we have to process the events manually if the current
        // thread is the Event Dispatch Thread (EDT).

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                EventQueue theQueue = getToolkit().getSystemEventQueue();

                while (!responseGiven) {
                    // This is essentially the body of EventDispatchThread
                    AWTEvent event = theQueue.getNextEvent();
                    Object src = event.getSource();

                    try {
                        // We cannot call theQueue.dispatchEvent, so I
                        // pasted its body here:
                        if (event instanceof ActiveEvent) {
                            ((ActiveEvent) event).dispatch();
                        } else if (src instanceof Component) {
                            ((Component) src).dispatchEvent(event);
                        } else if (src instanceof MenuComponent) {
                            ((MenuComponent) src).dispatchEvent(event);
                        } else {
                            logger.warning("unable to dispatch event: "
                                + event);
                        }
                    } finally {
                        continue;
                    }
                }
            } else {
                while (!responseGiven) {
                    wait();
                }
            }
        } catch (InterruptedException e) {}

        T tempResponse = response;
        response = null;
        responseGiven = false;

        return tempResponse;
    }


    /**
     * Used for Polymorphism in Recruit, Purchase, Train Dialogs
     */
    public void initialize() {}

    /**
     * Sets that no response has been given.
     */
    public void resetResponse() {
        response = null;
        responseGiven = false;
    }

    /**
     * Sets the <code>response</code> and wakes up any thread waiting
     * for this information.
     *
     * @param response The object that should be returned by
     *      {@link #getResponse}.
     */
    public synchronized void setResponse(T response) {
        this.response = response;
        responseGiven = true;
        logger.info("Response has been set to " + response);
        notifyAll();
    }
}
