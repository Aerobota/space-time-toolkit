package org.vast.stt.gui.widgets.bbox;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.vast.stt.apps.STTPlugin;
import org.vast.stt.data.DataProvider;
import org.vast.stt.scene.DataItem;
import org.vast.stt.util.SpatialExtent;

public class BboxWidget implements SelectionListener 
{
	DataItem dataItem;
	Text nlatText, slatText, wlonText, elonText;
	Combo formatCombo;
	Button fitBtn;
	//  Not sure I really need a persistent pointer to the currently selected 
	//  LlFormat.  Can just get this from formatCombo.  Remove if it's not needed.
	static final int DEGREES = 0;
	static final int RADIANS = 1;
	int currentLlFormat;
	private Group mainGroup; 
	
	public BboxWidget(Composite parent){
		init(parent);
	}
	
	public void init(Composite parent){
		//  Scroller
		final ScrolledComposite scroller = 
			new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		scroller.setExpandVertical(true);
		scroller.setExpandHorizontal(true);
		scroller.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

	    mainGroup = new Group(scroller, 0x0);
		mainGroup.setText("itemName");
		scroller.setContent(mainGroup);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 10;
		mainGroup.setLayout(layout);
		
		//  Create a Group for the compass Grid
		Composite compassGroup = new Composite(mainGroup, SWT.SHADOW_NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		compassGroup.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compassGroup.setLayout(gridLayout);
		//  Create Buttons
		Group nlatGroup = new Group(compassGroup, 0x0);
		nlatGroup.setText("northLat");
		FillLayout fl = new FillLayout();
		fl.marginHeight = 4;
		fl.marginWidth = 4;
		nlatGroup.setLayout(fl);
		nlatText = new Text(nlatGroup, SWT.RIGHT);
		Group wlonGroup = new Group(compassGroup, 0x0);
		wlonGroup.setText("westLon");
		fl = new FillLayout();
		fl.marginHeight = 4;
		fl.marginWidth = 4;
		wlonGroup.setLayout(fl);
		wlonText = new Text(wlonGroup, SWT.RIGHT);
		Label compassBtn = new Label(compassGroup, SWT.SHADOW_IN);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.CENTER;
		compassBtn.setLayoutData(gridData);
		ImageDescriptor descriptor = STTPlugin.getImageDescriptor("icons/compass1.gif");
		Image compImg = descriptor.createImage();
		compassBtn.setImage(compImg);
		//compassBtn.setImage(Image)
		Group elonGroup = new Group(compassGroup, 0x0);
		elonGroup.setText("eastLon");
		fl = new FillLayout();
		fl.marginHeight = 4;
		fl.marginWidth = 4;
		elonGroup.setLayout(fl);
		elonText = new Text(elonGroup, SWT.RIGHT);
		Group slatGroup = new Group(compassGroup, 0x0);
		slatGroup.setText("southLat");
		fl = new FillLayout();
		fl.marginHeight = 4;
		fl.marginWidth = 4;
		slatGroup.setLayout(fl);
		slatText = new Text(slatGroup, SWT.RIGHT);

		//  Layout Btns
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 3;
		nlatGroup.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = SWT.CENTER;
		wlonGroup.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = SWT.CENTER;
		elonGroup.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 3;
		slatGroup.setLayoutData(gridData);
		
		//  Format combo
		Composite formatGrp = new Composite(mainGroup, 0x0);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		formatGrp.setLayoutData(gridData);
		RowLayout formatLayout = new RowLayout(SWT.HORIZONTAL);
		formatGrp.setLayout(formatLayout);
		Label formatLbl = new Label(formatGrp, SWT.CENTER);
		formatLbl.setText("Format:");
		formatCombo = new Combo(formatGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
		formatCombo.addSelectionListener(this);
		//  May add options for dd'mm"ss later
		formatCombo.add("degrees");
		formatCombo.add("radians");
		formatCombo.select(0);
		currentLlFormat = DEGREES;  //  default to DEGREES 
		
		//  Fit Button
		fitBtn = new Button(mainGroup, SWT.PUSH);
		fitBtn.setText("Fit Item to View");
		fitBtn.addSelectionListener(this);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		fitBtn.setLayoutData(gridData);
		//  Set Default scroller size
		scroller.setMinSize(mainGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	public SpatialExtent getSpatitalExtent(){
		double nlat = getValue(nlatText);
		double slat = getValue(slatText);
		double wlon = getValue(wlonText);
		double elon = getValue(elonText);
		SpatialExtent ext = new SpatialExtent();
		ext.setMinX(wlon);
		ext.setMinY(slat);
		ext.setMaxX(elon);
		ext.setMaxY(nlat);
		return ext;
	}
	
	//  Use org.vast.stt.util.SpatialExtent, or Bbox here? 
	public void setSpatialExtent(SpatialExtent bbox){
		setValue(nlatText, bbox.getMaxY());
		setValue(slatText, bbox.getMinY());
		setValue(wlonText, bbox.getMinX());
		setValue(elonText, bbox.getMaxX());
	}
	
	public void setFormat(int formatIndex){
		if(formatIndex == currentLlFormat)
			return;
		currentLlFormat = formatIndex;
		SpatialExtent bbox = this.getSpatitalExtent();
		double minX = bbox.getMinX();
		double maxX = bbox.getMaxX();
		double minY = bbox.getMinY();
		double maxY = bbox.getMaxY();
		double factor = 1.0;
		switch(currentLlFormat){
			case DEGREES:
				factor = 180.0/Math.PI;
				break;
			case RADIANS:
				factor = Math.PI/180.0;  //UnitConversion.getFactorToSI("deg");
				break;
			default: 
				break;
		}
		minX *= factor;
		maxX *= factor;
		minY *= factor;
		maxY *= factor;
		bbox.setMinX(minX);
		bbox.setMinY(minY);
		bbox.setMaxX(maxX);
		bbox.setMaxY(maxY);
		this.setSpatialExtent(bbox);
	}
	
	public void setDataItem(DataItem item){
		this.dataItem = item;
		System.err.println("item " + item);
		mainGroup.setText(item.getName());
		DataProvider prov = item.getDataProvider();
		//  If provider is null, this widget isn't supported.
		if(prov!=null) {
			SpatialExtent ext = prov.getSpatialExtent();
			this.setSpatialExtent(ext);
		}
	}
	
	public double getValue(Text text){
		double value;
		try {
			value = Double.parseDouble(text.getText());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.err.println("ParseError: " + text.getText());
			return 0.0;
		}

		return value;
	}
		
	public void setValue(Text text, double d){
		String dubStr = Double.toString(d);
		text.setText(dubStr);
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		//System.err.println(e);
		if(e.widget == fitBtn){
			double nlat = getValue(nlatText);
			double slat = getValue(slatText);
			double wlon = getValue(wlonText);
			double elon = getValue(elonText);
			System.out.println("Fit: " + wlon + ", " + slat + " ==> " + elon + ", " + nlat);
		} else if (e.widget == formatCombo){
			//System.err.println("Selection Index = " + formatCombo.getSelectionIndex());
			this.setFormat(formatCombo.getSelectionIndex());
		}
	}
}