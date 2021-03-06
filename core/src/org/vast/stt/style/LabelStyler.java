/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "Space Time Toolkit".
 
 The Initial Developer of the Original Code is the VAST team at the
 University of Alabama in Huntsville (UAH). <http://vast.uah.edu>
 Portions created by the Initial Developer are Copyright (C) 2007
 the Initial Developer. All Rights Reserved.
 
 Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.stt.style;

import org.vast.data.AbstractDataBlock;
import org.vast.ows.sld.ScalarParameter;
import org.vast.ows.sld.Symbolizer;
import org.vast.ows.sld.TextSymbolizer;
import org.vast.stt.data.BlockListItem;


/**
 * <p><b>Title:</b><br/>
 * Label Styler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Converts source data to a sequence of LabelGraphic objects
 * that the renderer can access and render sequentially. 
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Nov 15, 2005
 * @version 1.0
 */
public class LabelStyler extends AbstractStyler implements DataStyler1D
{
    protected LabelGraphic label;
    protected TextSymbolizer symbolizer;
    protected int[] pointIndex = new int[1];
    protected int labelDensity = 10;
    protected int labelSpacing;
    protected BlockListItem constantBlock;
    protected boolean returnConstantGraphic;
    protected boolean returnConstantBlock;
    	
	
	public LabelStyler()
	{
        label = new LabelGraphic();
        constantBlock = new BlockListItem(null, null, null);
	}
    
    
    public LabelGraphic nextPoint()
    {
        // reset point values
        label.x = constantX;
        label.y = constantY;
        label.z = constantZ;
        
        if (returnConstantGraphic || nextItem())
        {
            returnConstantGraphic = false;
            
            // adjust geometry to fit projection
            if (projection != null)
                projection.adjust(geometryCrs, label);
            
            return label;
        }
        
        return null;
    }
    
    
    public int getNumPoints()
    {
        if (allConstant)
            return 1;
        
        if (dataLists[0].indexOffset == 0)
            return dataLists[0].blockIterator.getList().getSize();
        
        return 0;
    }
    
    
    public LabelGraphic getPoint(int u)
    {
        // reset point values
        label.x = constantX;
        label.y = constantY;
        label.z = constantZ;
        
        if (!allConstant)
        {    
            if (dataLists[0].indexOffset == 0)
            {
                AbstractDataBlock dataBlock = dataLists[0].blockIterator.getList().get(u);
                dataLists[0].blockIndexer.setData(dataBlock);
                dataLists[0].blockIndexer.reset();
                dataLists[0].blockIndexer.next();
            }
            else
            {
                pointIndex[0] = u;
                dataLists[0].blockIndexer.getData(pointIndex);
            }
        }
        
        // adjust geometry to fit projection
        if (projection != null)
            projection.adjust(geometryCrs, label);
        
        return label;
    }
    
    
    @Override
    public void computeBoundingBox()
    {
        this.resetIterators();
        PrimitiveGraphic point;
        
        while (nextBlock() != null)
            while ((point = nextPoint()) != null)
                addToExtent(point);
    }


    @Override
	public void updateDataMappings()
	{
        ScalarParameter param;
        String propertyName;
        Object value;
        
        // reset all parameters
        label = new LabelGraphic();
        this.clearAllMappers();
        
        // constantPoint is true for now, it will be set to
        // false if at least one of X,Y,Z properies has a mapper
        allConstant = true;
        
        // X,Y,Z are initialized to 0 by default
        constantX = constantY = constantZ = 0.0;
               
        // geometry X
        param = this.symbolizer.getGeometry().getX();
        updateMappingX(label, param);
        
        //geometry Y
        param = this.symbolizer.getGeometry().getY();
        updateMappingY(label, param);
        
        // geometry Z
        param = this.symbolizer.getGeometry().getZ();
        updateMappingZ(label, param);
        
        // geometry T
        param = this.symbolizer.getGeometry().getT();
        updateMappingT(label, param);
        
        // color - red 
        param = symbolizer.getFill().getColor().getRed();
        updateMappingRed(label, param);
        
        // color - green 
        param = symbolizer.getFill().getColor().getGreen();
        updateMappingGreen(label, param);
        
        // color - blue 
        param = symbolizer.getFill().getColor().getBlue();
        updateMappingBlue(label, param);
        
        // color - alpha 
        param = symbolizer.getFill().getColor().getAlpha();
        updateMappingAlpha(label, param);
        
        // label text
        param = this.symbolizer.getLabel();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                label.text = (String)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new LabelTextMapper(label, param.getMappingFunction()));              
                }
            }
        }
        
        // text size
        param = this.symbolizer.getFont().getSize();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                label.size = ((Float)value).intValue();
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new LabelSizeMapper(label, param.getMappingFunction()));              
                }
            }
        }
        
        // label orientation
        param = null;//this.symbolizer.getPlacement().getRotation();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                label.orientation = (Float)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new LabelOrientationMapper(label, param.getMappingFunction()));              
                }
            }
        }
        
        mappingsUpdated = true;
	}
	
	
	public TextSymbolizer getSymbolizer()
	{
		return symbolizer;
	}


	public void setSymbolizer(Symbolizer sym)
	{
		this.symbolizer = (TextSymbolizer)sym;
        this.setCrs(sym.getGeometry().getCrs());
	}
	
	
	public void accept(StylerVisitor visitor)
	{
        dataNode = dataItem.getDataProvider().getDataNode();
        
        if (dataNode.isNodeStructureReady())
        {
            if (!mappingsUpdated)
                updateDataMappings();
                        
    		visitor.visit(this);
        }
	}
    
    
    @Override
    public void resetIterators()
    {
        super.resetIterators();
        returnConstantBlock = allConstant;
        returnConstantGraphic = allConstant;
    }
    
    
    @Override
    public BlockListItem nextBlock()
    {
        if (returnConstantBlock)
        {
            returnConstantBlock = false;
            return constantBlock;
        }
        
        return super.nextBlock();
    }
}