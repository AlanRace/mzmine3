/*
 * Copyright 2006-2016 The MZmine 3 Development Team
 * 
 * This file is part of MZmine 3.
 * 
 * MZmine 3 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 3 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 3; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.gui.mainwindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mzmine.gui.MZmineGUI;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModule;
import io.github.mzmine.modules.MZmineRunnableModule;
import io.github.mzmine.parameters.ParameterSet;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;

public final class ModuleMenuItem extends MenuItem {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final StringProperty moduleClass = new SimpleStringProperty();

  @SuppressWarnings({"unchecked"})
  public ModuleMenuItem() {
    setOnAction(event -> {
      logger.info("Menu item activated: " + event);
      Class<? extends MZmineRunnableModule> moduleJavaClass;
      try {
        moduleJavaClass = (Class<? extends MZmineRunnableModule>) Class.forName(moduleClass.get());
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        MZmineGUI.displayMessage("Cannot find module class " + moduleClass.get());
        return;
      }

      MZmineModule module = MZmineCore.getModuleInstance(moduleJavaClass);

      if (module == null) {
        MZmineGUI.displayMessage("Cannot find module of class " + moduleClass.get());
        return;
      }

      ParameterSet moduleParameters =
          MZmineCore.getConfiguration().getModuleParameters(moduleJavaClass);

      logger.info("Setting parameters for module " + module.getName());
      String title = "Set parameters for " + module.getName();
      ButtonType exitCode = moduleParameters.showSetupDialog(title);
      if (exitCode != ButtonType.OK)
        return;

      ParameterSet parametersCopy = moduleParameters.clone();
      logger.debug("Starting module " + module.getName() + " with parameters " + parametersCopy);
      MZmineCore.runMZmineModule(moduleJavaClass, parametersCopy);

    });
  }

  public String getModuleClass() {
    return moduleClass.get();
  }

  public void setModuleClass(String newClass) {
    moduleClass.set(newClass);
  }

  public StringProperty moduleClassProperty() {
    return moduleClass;
  }

}
