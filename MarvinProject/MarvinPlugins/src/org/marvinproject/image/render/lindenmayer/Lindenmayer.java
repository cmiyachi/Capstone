/**
Marvin Project <2007-2010>

Initial version by:

Danilo Rosetto Munoz
Fabio Andrijauskas
Gabriel Ambrosio Archanjo

site: http://marvinproject.sourceforge.net

GPL
Copyright (C) <2007>  

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package org.marvinproject.image.render.lindenmayer;

import marvin.gui.MarvinAttributesPanel;
import marvin.gui.MarvinFilterWindow;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.util.MarvinAttributes;

/**
* @author Gabriel Ambr�sio Archhanjo
*/
public class Lindenmayer extends MarvinAbstractImagePlugin{

	private MarvinAttributesPanel	attributesPanel;
	private MarvinAttributes 		attributes;
	
	private Grammar 		grammar;
	private TurtleGraphics	turtle;
	private String			startText;
	
	private String RULES =		"start->G\n" + 	
								"G->F[-G][+G]FG\n"+
								"F->FF\n";
	
	
	@Override
	public void load() {
		attributes = getAttributes();
		attributes.set("rotationAngle", 25.7);
		attributes.set("initialAngle", 90.0);
		attributes.set("iterations", 9);
		attributes.set("rules", RULES);
		attributes.set("initialText", "G");
		
		grammar = new Grammar();
		turtle = new TurtleGraphics();
	}

	@Override
	public void process
	(
		MarvinImage imageIn, 
		MarvinImage imageOut, 
		MarvinAttributes out2,
		MarvinImageMask a_mask, 
		boolean mode
	) {
		
		String rules[] = null;
		String strRules = ((String)(attributes.get("rules")));
		if(strRules.contains("\n")){
			rules = ((String)(attributes.get("rules"))).split("\n");
		}
		else if(strRules.contains("&")){
			rules = ((String)(attributes.get("rules"))).split("&");
		}
		
		double initialAngle = (Double)attributes.get("initialAngle");
		double rotationAngle = (Double)attributes.get("rotationAngle");
		int iterations = (Integer)attributes.get("iterations");
		
		
		for(int i=0; i<rules.length; i++){
			addRule(rules[i]);
		}
		
		turtle.setStartPosition(0, 0, initialAngle);
		turtle.setRotationAngle(rotationAngle);
		
		imageOut.clearImage(0xFFFFFFFF);
		turtle.render(startText, grammar,iterations, imageOut);
		
	}

	@Override
	public MarvinAttributesPanel getAttributesPanel(){
		if(attributesPanel == null){
			attributesPanel = new MarvinAttributesPanel();
			attributesPanel.addLabel("lblIterations","iterations:");
			attributesPanel.addTextField("txtIterations","iterations", attributes);
			attributesPanel.newComponentRow();
			
			attributesPanel.addLabel("lblInitialAngle","initialAngle:");
			attributesPanel.addTextField("txtInitialAngle","initialAngle", attributes);
			attributesPanel.newComponentRow();
			
			attributesPanel.addLabel("lblRotationAngle","rotationAngle:");
			attributesPanel.addTextField("txtRotationAngle","rotationAngle", attributes);
			attributesPanel.newComponentRow();
			
			attributesPanel.addLabel("lblRules","rules:");
			attributesPanel.addTextArea("txtRules","rules", 8, 40, attributes);
		}
		return attributesPanel;
	}
	
	private void addRule(String rule){
		String r[] = rule.split("->");
		
		if(r[0].equals("start")){
			startText = r[1];
		}
		else{
			grammar.addRule(r[0], r[1]);
		}
	}
}
