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

package org.vast.stt.project.table;

import java.util.Hashtable;
import org.vast.xml.DOMHelper;
import org.vast.stt.project.XMLModuleReader;
import org.vast.stt.project.XMLReader;
import org.vast.stt.project.tree.DataTree;
import org.vast.stt.project.tree.DataTreeReader;
import org.w3c.dom.Element;


/**
 * <p><b>Title:</b>
 * Table Scene Reader
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Create TableScene object and read options and contents from project XML
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Sep 11, 2006
 * @version 1.0
 */
public class TableSceneReader extends XMLReader implements XMLModuleReader
{
    protected DataTreeReader dataTreeReader;
    
    
    public TableSceneReader()
    {
        dataTreeReader = new DataTreeReader();
    }
    
    
    @Override
    public void setObjectIds(Hashtable<String, Object> objectIds)
    {
        super.setObjectIds(objectIds);
        dataTreeReader.setObjectIds(objectIds);
    }


    public Object read(DOMHelper dom, Element sceneElt)
    {
        TableScene scene = new TableScene();
        registerObjectID(dom, sceneElt, scene);
        
        // set scene properties
        scene.setName(dom.getElementValue(sceneElt, "name"));
        
        // read data item list
        Element listElt = dom.getElement(sceneElt, "contents/DataList");
        dataTreeReader.setParentScene(scene);
        DataTree dataTree = dataTreeReader.readDataTree(dom, listElt);
        scene.setDataTree(dataTree);
        
        return scene;
    }
}
