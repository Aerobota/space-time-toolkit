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
import org.vast.ows.sld.LineSymbolizer;
import org.vast.ows.sld.ScalarParameter;
import org.vast.ows.sld.Symbolizer;
import org.vast.stt.data.BlockListItem;


/**
 * <p><b>Title:</b><br/>
 * Line Styler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Converts source data to a sequence of LineGraphic objects
 * that the renderer can access and render sequentially.
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Nov 15, 2005
 * @version 1.0
 */
public class LineStyler extends AbstractStyler implements DataStyler1D
{
    protected LineSymbolizer symbolizer;
    protected LinePointGraphic point;
    protected LineSegmentGraphic segment;
    protected int[] lineIndex = new int[1];
        
	
	public LineStyler()
	{
		point = new LinePointGraphic();
        segment = new LineSegmentGraphic();
	}
    
    
    public LineSegmentGraphic nextLineBlock()
    {
        ListInfo listInfo = dataLists[0];
        BlockListItem nextItem;
        
        // if no more items in the list, just return null
        if (!listInfo.blockIterator.hasNext())
        {
            clearBlockData();
            return null;
        }
    
        // otherwise get the next item
        nextItem = listInfo.blockIterator.next();
        
        // setup indexer with new data 
        AbstractDataBlock nextBlock = nextItem.getData();
        listInfo.blockIndexer.setData(nextBlock);
        listInfo.blockIndexer.reset();
        
        // see what's needed on this block
        prepareBlock(nextItem);
        
        // copy current item in the segment object
        segment.block = nextItem;
        
        return segment;
    }
    
    
    public LinePointGraphic nextPoint()
    {
        while (dataLists[0].blockIndexer.hasNext())
        {
            point.x = point.y = point.z = 0.0;            
            dataLists[0].blockIndexer.next();
            
            // get next one until time is ok
            //if (!checkTime(point.t))
            //    continue;
            
            // adjust geometry to fit projection
            if (projection != null)
                projection.adjust(geometryCrs, point);

            return point;
        }
        
        return null;
    }
    
    
    public int getNumPoints()
    {
        if (dataLists[0].indexOffset == 0)
            return dataLists[0].blockIterator.getList().getSize();
        else
            return segment.segmentSize;
    }
    
    
    public LinePointGraphic getPoint(int u)
    {
        point.x = point.y = point.z = 0.0;        
        
        if (dataLists[0].indexOffset == 0)
        {
            AbstractDataBlock dataBlock = dataLists[0].blockIterator.getList().get(u);
            dataLists[0].blockIndexer.setData(dataBlock);
            dataLists[0].blockIndexer.reset();
            dataLists[0].blockIndexer.next();
        }
        else
        {
            lineIndex[0] = u;
            dataLists[0].blockIndexer.getData(lineIndex);
        }
        
        // adjust geometry to fit projection
        if (projection != null)
            projection.adjust(geometryCrs, point);
        
        return point;
    }
    
    
    @Override
    public void computeBoundingBox()
    {
        this.resetIterators();
        PrimitiveGraphic point;
        
        while (nextLineBlock() != null)
            while ((point = nextPoint()) != null)
                addToExtent(point);
    }


    @Override
    public void updateDataMappings()
    {
        ScalarParameter param;
        String propertyName = null;
        Object value;
        
        // reset all parameters
        point = new LinePointGraphic();
        this.clearAllMappers();       
        
        // geometry breaks
        param = this.symbolizer.getGeometry().getBreaks();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericBreakMapper(point, param.getMappingFunction()));               
            }
        }
        
        // geometry X
        param = this.symbolizer.getGeometry().getX();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericXMapper(point, param.getMappingFunction()));                
            }
        }
        
        //geometry Y
        param = this.symbolizer.getGeometry().getY();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericYMapper(point, param.getMappingFunction()));
            }
        }
        
        // geometry Z
        param = this.symbolizer.getGeometry().getZ();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericZMapper(point, param.getMappingFunction()));
            }
        }
        
        // color - red 
        param = this.symbolizer.getStroke().getColor().getRed();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                point.r = (Float)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new GenericRedMapper(point, param.getMappingFunction()));
                }
            }
        }
        
        // color - green 
        param = this.symbolizer.getStroke().getColor().getGreen();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                point.g = (Float)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new GenericGreenMapper(point, param.getMappingFunction()));
                }
            }
        }
        
        // color - blue 
        param = this.symbolizer.getStroke().getColor().getBlue();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                point.b = (Float)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new GenericBlueMapper(point, param.getMappingFunction()));
                }
            }
        }
        
        // color - alpha 
        param = this.symbolizer.getStroke().getColor().getAlpha();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                point.a = (Float)value;
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new GenericAlphaMapper(point, param.getMappingFunction()));
                }
            }
        }
        
        // line width
        param = this.symbolizer.getStroke().getWidth();
        if (param != null)
        {
            if (param.isConstant())
            {
                value = param.getConstantValue();
                point.width = ((Float)value).intValue();
            }
            else
            {
                propertyName = param.getPropertyName();
                if (propertyName != null)
                {
                    addPropertyMapper(propertyName, new LineWidthMapper(point, param.getMappingFunction()));
                }
            }
        }
        
        dataLists[0].indexOffset = 0;
    }
    
    
	public LineSymbolizer getSymbolizer()
	{
		return symbolizer;
	}


	public void setSymbolizer(Symbolizer sym)
	{
		this.symbolizer = (LineSymbolizer)sym;
        this.setCrs(sym.getGeometry().getCrs());
	}


	public void accept(StylerVisitor visitor)
	{
        dataNode = dataItem.getDataProvider().getDataNode();

        if (dataNode.isNodeStructureReady())
        {
            if (dataLists.length == 0)
                updateDataMappings();
            
    		visitor.visit(this);
        }
	}
}