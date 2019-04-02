package awt.table.gui;
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/* 
 * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by 
 * TableDialogEditDemo.java.
 */

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.table.core.ColorMapperRCP;
import bacnet.table.core.ColorMapperRCPList;

public class HeatMapDoubleRenderer extends JLabel	implements TableCellRenderer {
	
	private static final long serialVersionUID = -2398479625198709031L;
	private boolean displayText = true;
	private boolean displayHeatMap = true;
	private ColorMapperRCPList colorMappers;
	
	// only for heatmap sRNA2011
	private boolean inocua = false;
	
    public HeatMapDoubleRenderer(boolean displayText,boolean displayHeatMap,ColorMapperRCPList colorMappers,boolean inocua) {
        this.displayText = displayText;
        this.displayHeatMap = displayHeatMap;
        this.colorMappers = colorMappers;
        this.inocua = inocua;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus,int row, int column) {
        String yo = value+"";
    	double expression = Double.parseDouble(yo);
        String header = table.getColumnName(column);
        ColorMapperRCP colorMapper = colorMappers.getCorrespondingMapper(header);
        setFont(colorMapper.getFontDouble());
        
        // display only two digits
        DecimalFormat twoDForm = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
        expression =  Double.valueOf(twoDForm.format(expression));
        
        //System.out.println("r "+row+" c "+column);
        if(displayText){
     	   if(expression == ExpressionMatrix.MISSING_VALUE) setText("");
     	   else{
     		   setText(expression+"");
     		   setForeground(ColorMapperRCP.swtColorToAwt(colorMapper.getTextColor()));
     	   }
        }else{
     	   setText("");
     	   setToolTipText(expression+"");
     	   setForeground(ColorMapperRCP.swtColorToAwt(colorMapper.getTextColor()));
        }
        
        if(displayHeatMap){
     	   //System.out.println(expression);
     	   if(expression == ExpressionMatrix.MISSING_VALUE) setBackground(Color.white);
     	   else{
     		   if(inocua) setBackground(ColorMapperRCP.swtColorToAwt(colorMapper.getInocuaColor(expression)));
     		   else{
     			   if(table.getColumnName(column).equals(GenomeNCBITools.EGDE_NAME)) setBackground(Color.black);
     			   else setBackground(colorMapper.parseColorAWT(expression));
     		   }
     	   }
        }
        else setBackground(Color.white);
        
        this.setHorizontalAlignment(SwingConstants.CENTER);
        
        
         
        return this;
       
    }
}
