package com.bur.BurReiterSnowflake;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/* Set up a DrawingPanel */
class drawPanel extends JPanel
{
    private BufferedImage bi;

    public drawPanel(int w, int h)
    {
        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        setSize(w, h);
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.white);
       
    }

    public void setPixel(int x, int y, int a, int r, int g, int b){
        bi.setRGB(x, y, (a<<24) |(r<<16) | (g<<8) | b);
    }


    public void paintComponent(Graphics g)
    {
        g.drawImage(bi, 0, 0, null);
    }
    
    public void clear()
    {
        for (int i=0; i<bi.getHeight(); i++)
            for (int j=0; j<bi.getWidth(); j++)
                bi.setRGB(j, i, 0);
    }
}
