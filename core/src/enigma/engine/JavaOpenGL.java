package enigma.engine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;

public class JavaOpenGL extends ApplicationAdapter {
	String shaderSrc_VS = 
			"#version 330 core\n" +
			"layout (location = 0) in vec3 aPos;\n" + 
			"void main()\n" +
			"{\n" +
			"    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0f);\n" +
			"}\n\0";
	;
	
	String shaderSrc_FS = 
			"#version 330 core\n" +
			"out vec4 FragColor;\n" + 
			"void main()\n" + 
			"{\n" +
			"    //always render an orange color\n" +
			"    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
			"}\n\0";
	;
	
	private SpriteBatch batch;
	private IntBuffer statusFlag;
	private IntBuffer VAO;
	private IntBuffer VBO_Triangle;
	private FloatBuffer trianglePoints;
	private int linkedShader; 

	@Override
	public void create() {
		batch = new SpriteBatch();
		
		//create VAO object
		VAO = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, VAO);
		Gdx.gl30.glBindVertexArray(VAO.get(0));
		
		//create the VBO object. 
		VBO_Triangle = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenBuffers(GL20.GL_ARRAY_BUFFER, VBO_Triangle);
		Gdx.gl30.glBindBuffer(GL20.GL_ARRAY_BUFFER, VBO_Triangle.get(0));
		
		//Triangle vertices
		trianglePoints = BufferUtils.newFloatBuffer(9);
		trianglePoints.put(0, -0.5f); trianglePoints.put(1, -0.5f); trianglePoints.put(2, -0.0f);
		trianglePoints.put(3, 0.5f); trianglePoints.put(4, -0.5f); trianglePoints.put(5, -0f);
		trianglePoints.put(6, 0f); trianglePoints.put(7, 0.5f); trianglePoints.put(8, 0f);
		
		//Buffer triangle vertice data. 
		Gdx.gl30.glBufferData(GL20.GL_ARRAY_BUFFER, trianglePoints.limit(), trianglePoints, GL20.GL_STATIC_DRAW);
		int floatByteSize = 4;
		Gdx.gl30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 3 * floatByteSize, 0);
		Gdx.gl30.glEnableVertexAttribArray(0);
		
		//bind VAO to null value to prevent overwriting current VAO configured state
		Gdx.gl30.glBindVertexArray(0);
		
		//create shaders
		statusFlag = BufferUtils.newIntBuffer(1);
		
		int vertShader = Gdx.gl30.glCreateShader(GL30.GL_VERTEX_SHADER);
		Gdx.gl30.glShaderSource(vertShader, shaderSrc_VS); //this is slightly different signature  //void glShaderSource(GLuint shader,GLsizei count,const GLchar **string,const GLint *length);
		Gdx.gl30.glCompileShader(vertShader);
		Gdx.gl30.glGetShaderiv(vertShader, GL20.GL_COMPILE_STATUS, statusFlag);
		if(statusFlag.get(0) == 0) {
			System.err.println("Failed to compile vertex shader.");
			//this signature is slightly different
			String error = Gdx.gl30.glGetShaderInfoLog(vertShader);
			System.err.println(error);
			Gdx.app.exit();
		}
		
		int fragShader = Gdx.gl30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
		Gdx.gl30.glShaderSource(fragShader, shaderSrc_FS);
		Gdx.gl30.glCompileShader(fragShader);
		Gdx.gl30.glGetShaderiv(fragShader, GL30.GL_COMPILE_STATUS, statusFlag);
		if(statusFlag.get(0) == 0) {
			System.err.println("Failed to compile fragment shader.");
			//this signature is slightly different
			String error = Gdx.gl30.glGetShaderInfoLog(fragShader);
			System.err.println(error);
			Gdx.app.exit();
		}
		
		linkedShader = Gdx.gl30.glCreateProgram();
		Gdx.gl30.glAttachShader(linkedShader, vertShader);
		Gdx.gl30.glAttachShader(linkedShader, fragShader);
		Gdx.gl30.glLinkProgram(linkedShader);
		
		Gdx.gl30.glGetProgramiv(linkedShader, GL30.GL_LINK_STATUS, statusFlag);
		if(statusFlag.get(0) == 0) {
			System.err.println("Error linking shader");
			String error = Gdx.gl30.glGetProgramInfoLog(linkedShader); //slightly different signature
			System.err.println(error);
			Gdx.app.exit();
		}
		
		//clean up temporaries 
		Gdx.gl30.glDeleteShader(vertShader);
		Gdx.gl30.glDeleteShader(fragShader);
		
		Gdx.gl30.glUseProgram(linkedShader);
		
		//no glPolygonMode, default values should be set anyways.
	}

	@Override
	public void render() {
		gdxIO();

		Gdx.gl30.glClearColor(0, 0, 0, 1);
		Gdx.gl30.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Gdx.gl.glViewport(10, 10, 400, 300);
		Gdx.gl30.glUseProgram(linkedShader);
		Gdx.gl30.glBindVertexArray(VAO.get(0));
		Gdx.gl30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3);
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		
		Gdx.gl30.glDeleteVertexArrays(1, VAO);
		Gdx.gl30.glDeleteBuffers(1, VBO_Triangle);
	}

	private void gdxIO() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

	}


}
