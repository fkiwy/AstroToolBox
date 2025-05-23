package astro.tool.box.panel;

import static astro.tool.box.main.ToolboxHelper.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

public class DualListBox extends JPanel {

	private final JList sourceList;
	private final JList destList;
	private final JButton showButton;
	private final JButton hideButton;
	private final JButton showAllButton;
	private final JButton hideAllButton;
	private final JButton resetButton;

	private final CustomListModel sourceListModel;
	private final CustomListModel destListModel;

	private List allElements;

	public DualListBox(int width, int height) {
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new GridBagLayout());

		JPanel globalLayout = new JPanel(new GridLayout(1, 3));
		globalLayout.setPreferredSize(new Dimension(width, height));
		add(globalLayout);

		sourceListModel = new CustomListModel();
		sourceList = new JList(sourceListModel);
		globalLayout.add(new JScrollPane(sourceList));

		JPanel buttonLayout = new JPanel(new GridLayout(5, 1));
		globalLayout.add(buttonLayout);

		showButton = new JButton(html("Show tab &raquo;"));
		showButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		showButton.addActionListener(new AddListener());
		buttonLayout.add(showButton);

		hideButton = new JButton(html("&laquo; Hide tab"));
		hideButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		hideButton.addActionListener(new RemoveListener());
		buttonLayout.add(hideButton);

		showAllButton = new JButton(html("Show all &raquo;"));
		showAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		showAllButton.addActionListener(new AddAllListener());
		buttonLayout.add(showAllButton);

		hideAllButton = new JButton(html("&laquo; Hide all"));
		hideAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		hideAllButton.addActionListener(new RemoveAllListener());
		buttonLayout.add(hideAllButton);

		resetButton = new JButton("Reset");
		resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		resetButton.addActionListener(new ResetListener());
		buttonLayout.add(resetButton);

		destListModel = new CustomListModel();
		destList = new JList(destListModel);
		globalLayout.add(new JScrollPane(destList));
	}

	private class AddListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			List selected = sourceList.getSelectedValuesList();
			addDestinationElements(selected);
			clearSourceSelected();
		}
	}

	private class RemoveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			List selected = destList.getSelectedValuesList();
			addSourceElements(selected);
			clearDestinationSelected();
		}
	}

	private class AddAllListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			destListModel.addAll(sourceListModel.getModel());
			sourceListModel.clear();
		}
	}

	private class RemoveAllListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			sourceListModel.addAll(destListModel.getModel());
			destListModel.clear();
		}
	}

	private class ResetListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			destListModel.setModel(allElements);
			sourceListModel.clear();
		}
	}

	public List getAllElements() {
		return allElements;
	}

	public void setAllElements(List allElements) {
		this.allElements = allElements;
	}

	public void clearSourceListModel() {
		sourceListModel.clear();
	}

	public void clearDestinationListModel() {
		destListModel.clear();
	}

	public void addSourceElements(ListModel newValue) {
		fillListModel(sourceListModel, newValue);
	}

	public void setSourceElements(ListModel newValue) {
		clearSourceListModel();
		addSourceElements(newValue);
	}

	public void addDestinationElements(ListModel newValue) {
		fillListModel(destListModel, newValue);
	}

	public void setDestinationElements(ListModel newValue) {
		clearDestinationListModel();
		addDestinationElements(newValue);
	}

	private void fillListModel(CustomListModel model, ListModel newValues) {
		int size = newValues.getSize();
		for (int i = 0; i < size; i++) {
			model.add(newValues.getElementAt(i));
		}
	}

	public void addSourceElements(List newValue) {
		fillListModel(sourceListModel, newValue);
	}

	public void setSourceElements(List newValue) {
		clearSourceListModel();
		addSourceElements(newValue);
	}

	public List getSourceElements() {
		return sourceListModel.getModel();
	}

	public void addDestinationElements(List newValue) {
		fillListModel(destListModel, newValue);
	}

	public void setDestinationElements(List newValue) {
		clearDestinationListModel();
		addDestinationElements(newValue);
	}

	public List getDestinationElements() {
		return destListModel.getModel();
	}

	private void fillListModel(CustomListModel model, List newValues) {
		model.addAll(newValues);
	}

	public Iterator sourceIterator() {
		return sourceListModel.iterator();
	}

	public Iterator destinationIterator() {
		return destListModel.iterator();
	}

	public void setSourceCellRenderer(ListCellRenderer newValue) {
		sourceList.setCellRenderer(newValue);
	}

	public ListCellRenderer getSourceCellRenderer() {
		return sourceList.getCellRenderer();
	}

	public void setDestinationCellRenderer(ListCellRenderer newValue) {
		destList.setCellRenderer(newValue);
	}

	public ListCellRenderer getDestinationCellRenderer() {
		return destList.getCellRenderer();
	}

	public void setVisibleRowCount(int newValue) {
		sourceList.setVisibleRowCount(newValue);
		destList.setVisibleRowCount(newValue);
	}

	public int getVisibleRowCount() {
		return sourceList.getVisibleRowCount();
	}

	public void setSelectionBackground(Color newValue) {
		sourceList.setSelectionBackground(newValue);
		destList.setSelectionBackground(newValue);
	}

	public Color getSelectionBackground() {
		return sourceList.getSelectionBackground();
	}

	public void setSelectionForeground(Color newValue) {
		sourceList.setSelectionForeground(newValue);
		destList.setSelectionForeground(newValue);
	}

	public Color getSelectionForeground() {
		return sourceList.getSelectionForeground();
	}

	private void clearSourceSelected() {
		List selected = sourceList.getSelectedValuesList();
		for (int i = selected.size() - 1; i >= 0; --i) {
			sourceListModel.removeElement(selected.get(i));
		}
		sourceList.getSelectionModel().clearSelection();
	}

	private void clearDestinationSelected() {
		List selected = destList.getSelectedValuesList();
		for (int i = selected.size() - 1; i >= 0; --i) {
			destListModel.removeElement(selected.get(i));
		}
		destList.getSelectionModel().clearSelection();
	}

}

class CustomListModel extends AbstractListModel {

	private List model;

	public CustomListModel() {
		model = new ArrayList();
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public Object getElementAt(int index) {
		return model.toArray()[index];
	}

	public void add(Object element) {
		if (model.add(element)) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	public void addAll(List elements) {
		model.addAll(elements);
		fireContentsChanged(this, 0, getSize());
	}

	public void clear() {
		model.clear();
		fireContentsChanged(this, 0, getSize());
	}

	public boolean contains(Object element) {
		return model.contains(element);
	}

	public Iterator iterator() {
		return model.iterator();
	}

	public boolean removeElement(Object element) {
		boolean removed = model.remove(element);
		if (removed) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	public void sort() {
		model.sort(Comparator.naturalOrder());
	}

	public List getModel() {
		return model;
	}

	public void setModel(List model) {
		this.model = model;
		fireContentsChanged(this, 0, getSize());
	}

}
