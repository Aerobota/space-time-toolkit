/***************************************************************
 (c) Copyright 2005, University of Alabama in Huntsville (UAH)
 ALL RIGHTS RESERVED

 This software is the property of UAH.
 It cannot be duplicated, used, or distributed without the
 express written consent of UAH.

 This software developed by the Vis Analysis Systems Technology
 (VAST) within the Earth System Science Lab under the direction
 of Mike Botts (mike.botts@atmos.uah.edu)
 ***************************************************************/

package org.vast.stt.data;

import java.util.Hashtable;

import org.ogc.cdm.common.*;
import org.vast.data.DataList;
import org.vast.unit.UnitConversion;
import org.vast.unit.UnitConverter;


/**
 * <p><b>Title:</b><br/>
 * SOS Data Handler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Handles parser events and copy data to DataNode.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 11, 2005
 * @version 1.0
 */
public class SWEDataHandler implements DataHandler
{
	protected Hashtable<DataComponent, UnitConverter> converters;
	protected DataNode dataNode;
    protected DataList dataList;
	

	public SWEDataHandler()
	{
		converters = new Hashtable<DataComponent, UnitConverter>();
	}
	
	
	public void setDataList(DataNode dataNode, DataList dataList)
	{
		this.dataNode = dataNode;
        this.dataList = dataList;
	}
	
	
	public void endData(DataComponent info, DataBlock data)
	{
        dataNode.addData(dataList, data);
	}


	public void endDataAtom(DataComponent info, DataBlock data)
	{
		DataType dataType = data.getDataType();
        
        if (dataType != DataType.UTF_STRING && dataType != DataType.ASCII_STRING)
        {
            UnitConverter converter = converters.get(info);
    		
    		if (converter == null)
    		{
    			String uom = (String)info.getProperty(DataComponent.UOM);
    			converter = UnitConversion.createConverterToSI(uom);
    	    	converters.put(info, converter);
    		}
    		
        	// convert to SI
    		double newVal = converter.convert(data.getDoubleValue());
    		data.setDoubleValue(newVal);
        }
	}


	public void endDataBlock(DataComponent info, DataBlock data)
	{
	}


	public void startData(DataComponent info)
	{
	}


	public void startDataBlock(DataComponent info)
	{
	}


    public void beginDataAtom(DataComponent info, DataBlock data)
    {       
    }
}