package fr.frozen.game;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

public class Font {
	private int texture;
	private int base;
	private int gap;
	private float r = 1, g = 1, b = 1, a = 1;
	
	public Font(String filename) throws IOException {
		this(filename, 10);
	}
	
	public Font(String filename, int gap) throws IOException {
		this.gap = gap;
		loadTexture(filename);
		buildFont();
	}
	
	public void setColor(float r, float g, float b) {
		setColor(r,g,b,this.a);
	}
	
	public void setAlpha(float val) {
		this.a = val;
	}
	
	public void setRed(float val) {
		this.r = val;
	}
	
	public void setGreen(float val) {
		this.g = val;
	}
	
	public void setBlue(float val) {
		this.b = val;
	}
	
	public void setColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public int getCharWidth() {
		return gap+1;
	}
	
	public int getCharHeight() {
		return 16;
	}
	
	public void glPrint(String msg, float x, float y) {
		glPrint(msg,x,y,0);
	}
	
	public void glPrint(String msg, float x, float y, int set) {
		glPrint(msg,x,y,set,1);
	}
	
	public void glPrint(String msg, float x, float y,  int set, float scale) {   // Where The Printing Happens
        if (set>1) {
            set=1;
        }
        GL11.glPushMatrix();                                     // Store The Modelview Matrix
        GL11.glLoadIdentity();                                   // Reset The Modelview Matrix

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        
       /*int blendsrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int blenddst = GL11.glGetInteger(GL11.GL_BLEND_DST);*/
        
        //GL11.glColor3f(0,0,0);
        //GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE_MINUS_DST_COLOR);
        //GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE_MINUS_SRC_COLOR);
        
        GL11.glColor4f(r, g, b, a);
        
        GL11.glTranslatef(x, y, 0);                              // Position The Text (0,0 - Bottom Left)
        GL11.glScalef(scale, scale, 1);
        int baseOffset = base - 32 + (128 * set);                // Choose The Font Set (0 or 1)
        
        for(int i=0;i<msg.length();i++) {
            GL11.glCallList(baseOffset + msg.charAt(i));
            GL11.glTranslatef(1.0f, 0.0f, 0.0f);
        }
        
      /*  GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ZERO);
        GL11.glBlendFunc(blendsrc, blenddst);*/
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();                                      // Restore The Old Projection Matrix
    }
	
	
	private void loadTexture(String filename) throws IOException {
		texture = TextureLoader.getInstance().getTexture(filename).getTextureID();
    }
	
	private void buildFont() {                                  // Build Our Font Display List
        float   cx;                                             // Holds Our X Character Coord
        float   cy;                                             // Holds Our Y Character Coord

        base = GL11.glGenLists(256);                            // Creating 256 Display Lists
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);     // Select Our Font Texture

        for (int i=0;i<256;i++) {                               // Loop Through All 256 Lists
            cx = ((float)(i % 16)) / 16.0f;                     // X Position Of Current Character
            cy = ((float)((255 - i) / 16)) / 16.0f;                     // Y Position Of Current Character

            GL11.glNewList(base + i, GL11.GL_COMPILE);          // Start Building A List
            GL11.glBegin(GL11.GL_QUADS);                        // Use A Quad For Each Character
            
            GL11.glTexCoord2f(cx, 1 - cy - 0.0625f);            // Texture Coord (Bottom Left)
            GL11.glVertex2i(0, 0);                              // Vertex Coord (Bottom Left)
            
            GL11.glTexCoord2f(cx + 0.0625f, 1 - cy - 0.0625f);  // Texture Coord (Bottom Right)
            GL11.glVertex2i(16,0);                              // Vertex Coord (Bottom Right)
            
            GL11.glTexCoord2f(cx + 0.0625f, 1 - cy);            // Texture Coord (Top Right)
            GL11.glVertex2i(16,16);                             // Vertex Coord (Top Right)
            
            GL11.glTexCoord2f(cx, 1 - cy);                      // Texture Coord (Top Left)
            GL11.glVertex2i(0, 16);                             // Vertex Coord (Top Left)
            
            GL11.glEnd();                                       // Done Building Our Quad (Character)
            //GL11.glTranslatef(10.0f, 0.0f, 0.0f);               // Move To The Right Of The Character
            GL11.glTranslatef(gap, 0, 0);
            GL11.glEndList();                                   // Done Building The Display List
        }                                                       // Loop Until All 256 Are Built
        

    }
	
	/*private void cleanup() {
		GL11.glDeleteLists(base, 256);                            // Delete All 256 Display Lists
		Display.destroy();
	}*/
}
