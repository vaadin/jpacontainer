package com.vaadin.demo.jpaaddressbook;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class JpaAddressbookApplication extends Application {

	@Override
	public void init() {
		Window window = new Window();
		window.addComponent(new Label("It works!"));
		setMainWindow(window);
	}

}
