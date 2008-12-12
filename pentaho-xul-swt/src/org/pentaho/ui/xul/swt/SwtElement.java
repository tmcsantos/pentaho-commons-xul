package org.pentaho.ui.xul.swt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulContainer;
import org.pentaho.ui.xul.XulDomException;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulComponent;
import org.pentaho.ui.xul.util.Orient;

public class SwtElement extends AbstractXulComponent {
  private static final long serialVersionUID = -4407080035694005764L;

  // Per XUL spec, STRETCH is the default align value.
  private SwtAlign align = SwtAlign.STRETCH;

  protected Orient orient = Orient.HORIZONTAL;

  private int flex = 0;

  protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

  public SwtElement(String tagName) {
    super(tagName);
  }

  public int getFlex() {
    return flex;
  }

  public void setFlex(int flex) {
    this.flex = flex;
  }


  public void setOrient(String orientation) {
    orient = Orient.valueOf(orientation.toUpperCase());
  }

  public String getOrient() {
    return orient.toString();
  }

  public Orient getOrientation() {
    return Orient.valueOf(getOrient());
  }

  @Override
  /**
   * This method attempts to follow the XUL rules
   * of layouting, using an SWT GridLayout. Any deviations 
   * from the general rules are applied in overrides to this method. 
   */
  public void layout() {
    super.layout();
    if (this instanceof XulDeck) {
      return;
    }

    if (!(getManagedObject() instanceof Composite)) {
      return;
    }

    Composite container = (Composite) getManagedObject();

    // Total all flex values.
    // If every child has a flex value, then the GridLayout's columns
    // should be of equal width. everyChildIsFlexing gives us that boolean. 

    int totalFlex = 0;
    int thisFlex = 0;
    boolean everyChildIsFlexing = true;

    for (Object child : this.getChildNodes()) {
      thisFlex = ((SwtElement) child).getFlex();
      if (thisFlex <= 0) {
        everyChildIsFlexing = false;
      }
      totalFlex += thisFlex;
    }

    // By adding the total flex "points" with the number
    // of child controls, we get a close relative size, using
    // columns in the GridLayout. 

    switch (orient) {
      case HORIZONTAL:
        int columnCount = this.getChildNodes().size() + totalFlex;
        container.setLayout(new GridLayout(columnCount, everyChildIsFlexing));
        break;
      case VERTICAL:
        container.setLayout(new GridLayout());
        break;
    }

    for (Object child : this.getChildNodes()) {

      SwtElement swtChild = (SwtElement) child;

      // some children have no object they are managing... skip these kids!

      Object mo = swtChild.getManagedObject();
      if (mo == null || !(mo instanceof Control) || swtChild instanceof XulDialog){
        continue;
      }

      Control c = (Control) swtChild.getManagedObject();

      GridData data = new GridData();

      // How many columns or rows should the control span? Use the flex value plus
      // 1 "point" for the child itself. 

      data.horizontalSpan = orient.equals(Orient.HORIZONTAL) ? swtChild.getFlex() + 1 : 1;
      data.verticalSpan = orient.equals(Orient.VERTICAL) ? swtChild.getFlex() + 1 : 1;

      // In XUL, flex defines how the children grab the excess space 
      // in the container - therefore, we need to grab the excess space... 

      
      switch (orient) {
        case HORIZONTAL:
          data.verticalAlignment = SWT.FILL;
          data.grabExcessVerticalSpace = true;
          break;
        case VERTICAL:
          data.horizontalAlignment = SWT.FILL;
          data.grabExcessHorizontalSpace = true;
          break;
      }
      
      
      if (swtChild.getFlex() > 0) {
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;

        // In XUL, if align attribute is left off, the 
        // default is to fill available space ... (ALIGN=STRETCH)

        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
      } 

      

      // And finally, deal with the align attribute...
      // Align is the PARENT'S attribute, and affects the 
      // opposite direction of the orientation.

      if (swtChild instanceof XulContainer && ((XulContainer) swtChild).getAlign() != null) {
        SwtAlign swtAlign = SwtAlign.valueOf(((XulContainer) swtChild).getAlign().toString());
        
        if (orient.equals(Orient.HORIZONTAL)) {
          data.verticalAlignment = swtAlign.getSwtAlign();
          if (data.verticalAlignment == SWT.FILL) {
            data.grabExcessVerticalSpace = true;
          }
        } else { //Orient.VERTICAL
          data.horizontalAlignment = swtAlign.getSwtAlign();
          if (data.horizontalAlignment == SWT.FILL) {
            data.grabExcessHorizontalSpace = true;
          }
        }
      }
      c.setLayoutData(data);
    }
    container.layout(true);
  }

  @Override
  /**
   * Important to understand that when using this method in the 
   * SWT implementation:
   * 
   * SWT adds new children positionally based on add order. This
   * means that a child that was added third in a list of 5 can't be 
   * "replaced" to its third position in the dialog, it can only 
   * be added to the end of the child list. 
   * 
   * Major SWT limitation. Replacement can only be used in 
   * a limited number of cases. 
   */
  public void replaceChild(XulComponent oldElement, XulComponent newElement) throws XulDomException {

    super.replaceChild(oldElement, newElement);
    Widget thisWidget = (Widget) oldElement.getManagedObject();
    if (!thisWidget.isDisposed()) {
      thisWidget.dispose();
    }
    this.addComponent(newElement);
    ((Control) newElement.getManagedObject()).setParent((Composite) this.getManagedObject());

    layout();
  }

  public void setOnblur(String method) {
    throw new NotImplementedException();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    changeSupport.removePropertyChangeListener(listener);
  }

  public boolean isDisabled() {
    throw new NotImplementedException();
  }

  public void setDisabled(boolean disabled) {
    //throw new NotImplementedException();
  }

  public void adoptAttributes(XulComponent component) {
    throw new NotImplementedException();
  }

}