/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codex.dreamraid;

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;

/**
 *
 * @author gary
 */
public class Functions {
	
	public static final String
			CHARACTER_MOVEMENT_GROUP = "character_movement_group";
	
	public static final FunctionId
			WALK = new FunctionId(Functions.CHARACTER_MOVEMENT_GROUP, "walk"),
			STRAFE = new FunctionId(Functions.CHARACTER_MOVEMENT_GROUP, "strafe"),
			JUMP = new FunctionId(Functions.CHARACTER_MOVEMENT_GROUP, "jump"),
			SPRINT = new FunctionId(Functions.CHARACTER_MOVEMENT_GROUP, "sprint"),
			ATTACK = new FunctionId(Functions.CHARACTER_MOVEMENT_GROUP, "attack");
	
	public static void initialize(InputMapper im) {
		im.map(WALK, InputState.Positive, KeyInput.KEY_W);
		im.map(WALK, InputState.Negative, KeyInput.KEY_S);
		im.map(STRAFE, InputState.Positive, KeyInput.KEY_D);
		im.map(STRAFE, InputState.Negative, KeyInput.KEY_A);
		im.map(JUMP, KeyInput.KEY_SPACE);
		im.map(SPRINT, KeyInput.KEY_LSHIFT);
		im.map(ATTACK, Button.MOUSE_BUTTON1);
	}
	
}
