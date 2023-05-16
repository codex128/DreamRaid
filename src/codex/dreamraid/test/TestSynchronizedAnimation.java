/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.dreamraid.test;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.ArmatureMask;
import com.jme3.anim.SkinningControl;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author gary
 */
public class TestSynchronizedAnimation extends SimpleApplication {
	
	AnimComposer anim;
	float mytime = 0f;
	
	public static void main(String[] args) {
		new TestSynchronizedAnimation().start();
	}
	
	@Override
	public void simpleInitApp() {
	
		Spatial person = assetManager.loadModel("Models/characters/person.j3o");
		rootNode.attachChild(person);
		
		anim = person.getControl(AnimComposer.class);
		SkinningControl skin = person.getControl(SkinningControl.class);
		anim.makeLayer("body", ArmatureMask.createMask(skin.getArmature(), "lower_body"));
		ArmatureMask legmask = new ArmatureMask();
		legmask.addFromJoint(skin.getArmature(), "legs_main");
		legmask.addBones(skin.getArmature(), "main");
		anim.makeLayer("legs", legmask);
		anim.setCurrentAction("chop", "body");
		anim.setCurrentAction("run_legs", "legs");
		
		rootNode.addLight(new DirectionalLight(new Vector3f(1f, -1f, 1f)));
		rootNode.addLight(new DirectionalLight(new Vector3f(-1f, 1f, -1f)));
		
	}	
	@Override
	public void simpleUpdate(float tpf) {
		if ((mytime += tpf) < 4f && mytime > 3.3f) {
			double time = anim.getTime("legs");
			anim.setCurrentAction("run_body", "body");
			anim.setTime("body", time);
			mytime = 100f;
		}
	}
	
}
