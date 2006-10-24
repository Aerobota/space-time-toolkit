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

package org.vast.stt.renderer.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.vast.stt.style.GridPatchGraphic;
import org.vast.stt.style.GridPointGraphic;
import org.vast.stt.style.RasterTileGraphic;
import org.vast.stt.style.TextureStyler;
import org.vast.stt.style.TexturePatchGraphic;


/**
 * <p><b>Title:</b>
 * GL Render Texture
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Runnable for rendering mapped textures.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Aug 2, 2006
 * @version 1.0
 */
public class GLRenderTexture extends GLRunnable
{
    protected TextureStyler styler;
    protected TexturePatchGraphic patch;
    protected boolean normalizeCoords;
    
    
    public GLRenderTexture(GL gl, GLU glu)
    {
        this.gl = gl;
        this.glu = glu;
    }
    
    
    public void setStyler(TextureStyler styler)
    {
        this.styler = styler;        
    }

    
    @Override
    public void run()
    {
        GridPointGraphic point;
        float uScale = 1.0f;
        float vScale = 1.0f;
        int count = 0;
                
        RasterTileGraphic tex = patch.getTexture();
        GridPatchGraphic grid = patch.getGrid();
                
        // select fill mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        gl.glDisable(GL.GL_CULL_FACE);            
        gl.glColor4f(1.0f, 1.0f, 1.0f, tex.opacity);
        
        // compute tex coordinate scale (for padded textures)
        if (tex.widthPadding != 0 || tex.heightPadding != 0)
        {
            uScale = (float)tex.width / (float)(tex.width + tex.widthPadding);
            vScale = (float)tex.height / (float)(tex.height + tex.heightPadding);
        }
        
        do
        {
            // loop through all grid points
            for (int v = 0; v < grid.length-1; v++)
            {
                gl.glBegin(GL.GL_QUAD_STRIP);
                            
                for (int u = 0; u < grid.width; u++)
                {
                    for (int p=0; p<2; p++)
                    {                    
                        point = styler.getGridPoint(u, v+p, uScale, vScale, normalizeCoords);
                        
                        if (point.graphBreak)
                        {
                            gl.glEnd();
                            gl.glBegin(GL.GL_QUAD_STRIP);
                            if (p == 0) u--;
                            point.graphBreak = false;
                            break;
                        }
                        
                        gl.glTexCoord2f(point.tx, point.ty);
                        gl.glVertex3d(point.x, point.y, point.z);
                    }
                }
                
                gl.glEnd();
            }
            
            count++;
            if (count == blockCount)
                break;
        }
        while ((patch = styler.nextTile()) != null);
        
        blockCount = count;
    }
}
