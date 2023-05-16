package codex.dreamraid;

import codex.jmeutil.anim.AnimState;
import codex.jmeutil.anim.AnimationStackControl;
import codex.jmeutil.anim.SynchronizedAnimState;
import codex.jmeutil.anim.TargetTween;
import codex.jmeutil.character.OrbitalCamera;
import codex.jmeutil.scene.SceneGraphIterator;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.ArmatureMask;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.AbstractTween;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.LinearBlendSpace;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.mathd.Vec3i;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnalogFunctionListener, StateFunctionListener {
	
	BetterCharacterControl bcc;
	AnimComposer anim;
	AnimationStackControl asc;
	OrbitalCamera camera;
	Vec3i inputdirection = new Vec3i(0, 0, 0);
	float walkspeed = 30f;
	float sprintfactor = 1f;
	boolean sprinting = false;
	
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
		
		flyCam.setMoveSpeed(50f);
		
		// lemur initialization
		GuiGlobals.initialize(this);
		InputMapper im = GuiGlobals.getInstance().getInputMapper();
		Functions.initialize(im);
		GuiGlobals.getInstance().setCursorEventsEnabled(false);
		
		// states
		BulletAppState bulletapp = new BulletAppState();
		//bulletapp.setDebugEnabled(true);
		stateManager.attachAll(bulletapp);
		
		// player
		bcc = new BetterCharacterControl(1f, 5f, 200f);
		bcc.setGravity(new Vector3f(0f, -100f, 0f));
		bcc.getRigidBody().setProtectGravity(true);
		bcc.setJumpForce(new Vector3f(0f, 2000f, 0f));
		Spatial pspat = assetManager.loadModel("Models/characters/person.j3o");
		anim = pspat.getControl(AnimComposer.class);
		SkinningControl skin = pspat.getControl(SkinningControl.class);
		pspat.addControl(bcc);
		rootNode.attachChild(pspat);
		getPhysicsSpace().add(bcc);
		Spatial sword = assetManager.loadModel("Models/characters/sword.j3o");
		sword.setLocalScale(.4f);
		skin.getAttachmentsNode("hand.R").attachChild(sword);
		Spatial shield = assetManager.loadModel("Models/characters/shield.j3o");
		shield.setLocalScale(1f);
		skin.getAttachmentsNode("hand.L").attachChild(shield);
		
		// animation
		anim.makeLayer("body", ArmatureMask.createMask(skin.getArmature(), "lower_body"));
		ArmatureMask legmask = new ArmatureMask();
		legmask.addFromJoint(skin.getArmature(), "legs_main");
		legmask.addBones(skin.getArmature(), "main");
		anim.makeLayer("legs", legmask);
		anim.setCurrentAction("idle_body", "body");
		anim.setCurrentAction("idle_legs", "legs");
		anim.action("run_body").setSpeed(1.5);
		anim.action("run_legs").setSpeed(1.5);
		anim.actionBlended("myBlend", new LinearBlendSpace(2, 0), "chop", "idle_body");
		
		// animation states
		asc = new AnimationStackControl();
		anim.actionSequence("chop_once", anim.action("chop"), anim.action("myBlend"), new TargetTween<AnimationStackControl>(asc) {
			@Override
			protected void invoke(AnimationStackControl target) {
				target.enableState("chop", false);
			}
		});
		AnimState idle = asc.add(new AnimState("idle"));
		idle.add("body", "idle_body");
		idle.add("legs", "idle_legs");
		AnimState run = asc.add(new SynchronizedAnimState("run", false));
		run.add("body", "run_body");
		run.add("legs", "run_legs");
		AnimState slash = asc.add(new AnimState("chop", false));
		slash.add("body", "chop_once");
		pspat.addControl(asc);
		
		// camera
		camera = new OrbitalCamera(cam, im);
		camera.getDistanceDomain().set(25f, 25f);
		camera.setOffsets(new Vector3f(0f, 5f, 0f));
		im.activateGroup(OrbitalCamera.INPUT_GROUP);
		pspat.addControl(camera);
		
		// scene
		Spatial scene = assetManager.loadModel("Models/castle_gates.j3o");
		rootNode.attachChild(scene);
		SceneGraphIterator iterator = new SceneGraphIterator(scene);
		for (Spatial spatial : iterator) {
			
			String trigger = spatial.getUserData("trigger");
			if (trigger != null) {
				CollisionShape shape = CollisionShapeFactory.createBoxShape(spatial);
				GhostControl trig = new GhostControl(shape);
				spatial.addControl(trig);
				getPhysicsSpace().add(trig);
				spatial.setCullHint(Spatial.CullHint.Always);
				iterator.ignoreChildren();
				continue;
			}
			
			if (spatial instanceof Geometry) {
				RigidBodyControl rbc = new RigidBodyControl(0f);
				spatial.addControl(rbc);
				getPhysicsSpace().add(rbc);
			}
			
			if (spatial.getName().equals("start")) {
				bcc.warp(spatial.getWorldTranslation());
			}
			
		}
		
		// lighting (temporary)
		rootNode.addLight(new DirectionalLight(new Vector3f(1f, -1f, 1f)));
		rootNode.addLight(new DirectionalLight(new Vector3f(-1f, 1f, -1f)));
		
		im.addAnalogListener(this, Functions.WALK, Functions.STRAFE, Functions.JUMP);
		im.addStateListener(this, Functions.ATTACK, Functions.SPRINT);
		im.activateGroup(Functions.CHARACTER_MOVEMENT_GROUP);
		
    }
    @Override
    public void simpleUpdate(float tpf) {
		Vector3f dir = camera.getPlanarCameraDirection();
		Vector3f right = camera.getCamera().getLeft().negateLocal();
		dir.multLocal(getCharacterSpeed()*inputdirection.z).addLocal(right.multLocal(getCharacterSpeed()*inputdirection.x));
		if (dir.lengthSquared() == 0f) {
			if (bcc.getWalkDirection(new Vector3f()).lengthSquared() > 0f) {
				bcc.setWalkDirection(dir);
				asc.enableState("run", false);
			}
		}
		else {
			bcc.setWalkDirection(dir);
			bcc.setViewDirection(dir);
			asc.enableState("run", true);
		}
		if (inputdirection.y > 0) {
			bcc.jump();
		}
		inputdirection.set(0, 0, 0);
	}
    @Override
    public void simpleRender(RenderManager rm) {}	
	@Override
	public void valueActive(FunctionId func, double value, double tpf) {
		if (func == Functions.WALK) {
			inputdirection.z = sign(value);
		}
		else if (func == Functions.STRAFE) {
			inputdirection.x = sign(value);
		}
		else if (func == Functions.JUMP) {
			inputdirection.y = 1;
		}
	}
	@Override
	public void valueChanged(FunctionId func, InputState value, double tpf) {
		if (func == Functions.SPRINT) {
			sprinting = value.equals(InputState.Positive);
		}
		else if (func == Functions.ATTACK && value == InputState.Positive) {
			asc.enableState("chop", true);
		}
	}
	
	private PhysicsSpace getPhysicsSpace() {
		return stateManager.getState(BulletAppState.class).getPhysicsSpace();
	}
	private float getCharacterSpeed() {
		return walkspeed*(sprinting ? sprintfactor : 1f);
	}
	
	private static int sign(double n) {
		if (n > 0) return 1;
		else if (n < 0) return -1;
		else return 0;
	}
	
}
