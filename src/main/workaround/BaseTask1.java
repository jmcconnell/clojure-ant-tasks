package workaround;

public class BaseTask1 extends org.apache.tools.ant.Task
{
	static
	{
		Thread.currentThread()
			.setContextClassLoader(BaseTask1.class.getClassLoader());
	}
}
