package com.example.poshaisan;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

/**
 * Custom TableCell implementation that allows editing of Integer values in a
 * TableView.
 */
public class IntegerEditingCell extends TableCell<InventoryItem, Integer> {

    // Fields --------------------------------------------------

    private TextField textField;

    // Methods -------------------------------------------------

    /**
     * Default constructor for IntegerEditingCell.
     */
    public IntegerEditingCell() {
    }

    /**
     * Starts editing the cell by displaying a TextField.
     */
    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.selectAll();
    }

    /**
     * Cancels the editing of the cell and restores the original value.
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    /**
     * Updates the item in the cell and sets the appropriate graphic or text
     * display.
     *
     * @param item  The Integer value to update in the cell.
     * @param empty Indicates whether this cell represents any domain
     *              data.
     */
    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    /**
     * Creates a TextField for editing the cell's value.
     */
    private void createTextField() {
        textField = new TextField(getString());

        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnAction(e -> commitOrCancelEdit(textField.getText()));

        textField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                commitOrCancelEdit(textField.getText());
            }
        });

        textField.textProperty()
                 .addListener((observable, oldValue, newValue) -> {
                     if (!newValue.matches("\\d*")) {
                         textField.setText(newValue.replaceAll("[^\\d]", ""));
                     }
                 });
    }

    /**
     * Commits the edit if the text is a valid integer or cancels it if the text
     * is empty.
     *
     * @param text The text to be committed or used to cancel the edit.
     */
    void commitOrCancelEdit(String text) {
        if (text.isEmpty()) {
            cancelEdit();
        } else {
            commitEdit(Integer.parseInt(text));
        }
    }

    /**
     * Returns the string representation of the cell's item.
     *
     * @return The string representation of the item.
     */
    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
