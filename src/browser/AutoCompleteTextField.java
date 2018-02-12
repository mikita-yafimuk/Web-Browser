package browser;

import javafx.beans.InvalidationListener;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AutoCompleteTextField {

	private SortedSet<String> entries = new TreeSet<>();
	private final ContextMenu contextMenu = new ContextMenu();
	private int maximumEntries = 15;
	private final StringBuilder stringBuilder = new StringBuilder();
	private TextField textField;

	public void bindAutoCompletion(TextField textField , int maximumEntries , boolean addKeyListener ,
								   SortedSet<String> sortedSet) {
		entries = sortedSet;
		bindAutoCompletion(textField, maximumEntries, addKeyListener);
	}

	public void bindAutoCompletion(TextField textField , int maximumEntries , boolean addKeyListener) {
		this.textField = textField;
		this.maximumEntries = maximumEntries <= 0 ? 10 : maximumEntries;

		textField.textProperty().addListener(textListener);
		textField.focusedProperty().addListener(focusListener);
	}

	private final InvalidationListener textListener = v -> {
		if (textField.getText().length() == 0 || entries.isEmpty())
			contextMenu.hide();
		else {
			fillPopup();
			if (!contextMenu.isShowing()) {
				contextMenu.show(textField, Side.BOTTOM, 0, 0);
				// Request focus on first item
				if (!contextMenu.getItems().isEmpty())
					contextMenu.getSkin().getNode().lookup(".menu-item:nth-child(1)").requestFocus();
			}
		}

	};

	private final InvalidationListener focusListener = v -> {
		stringBuilder.delete(0, stringBuilder.length());
		contextMenu.hide();
	};

	/*Filling context menu with entries*/
	private void fillPopup() {
		contextMenu.getItems().clear();
		String text = textField.getText().toLowerCase();
		
		// Filter the first maximumEntries matching the text
		contextMenu.getItems().addAll(entries.stream().filter(string -> string.toLowerCase().
				contains(text.toLowerCase())).limit(maximumEntries).map(MenuItem::new).collect(Collectors.toList()));
		contextMenu.getItems().forEach(item -> item.setOnAction(a -> {
			textField.setText(item.getText());
			textField.positionCaret(textField.getLength());
		}));
	}
}
