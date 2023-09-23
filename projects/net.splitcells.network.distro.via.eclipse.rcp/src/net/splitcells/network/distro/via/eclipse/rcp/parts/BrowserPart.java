package net.splitcells.network.distro.via.eclipse.rcp.parts;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BrowserPart {

	@Inject
	private MPart part;
	@Inject
	private Display display;
	private Text address;
	private Browser browser;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		final var urlBar = new Composite(parent, SWT.NONE);
		final var rLayout = new GridLayout(3, false);
		urlBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		urlBar.setLayout(rLayout);

		final Button refreshButton = new Button(urlBar, SWT.PUSH);
		refreshButton.setText("↻");
		final Button urlUpdateButton = new Button(urlBar, SWT.PUSH);
		urlUpdateButton.setText("+");

		address = new Text(urlBar, SWT.BORDER | SWT.SINGLE);
		address.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		address.setText("http://splitcells.net");
		address.setEditable(true);

		browser = new Browser(parent, SWT.DEFAULT);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		updateUrl();

		refreshButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshBrowser();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing needs to be done.

			}

		});
		urlUpdateButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateUrl();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing needs to be done.

			}

		});
		address.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					updateUrl();
				}
			}
		});
	}

	private void refreshBrowser() {
		display.asyncExec(() -> {
			address.setText(browser.getUrl());
			browser.refresh();
		});
	}

	private void updateUrl() {
		display.asyncExec(() -> {
			browser.setUrl(address.getText());
			browser.refresh();
		});
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}
}