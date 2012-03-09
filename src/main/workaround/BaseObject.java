package workaround;

public class BaseObject {
  static {
    Thread.currentThread()
			.setContextClassLoader(BaseObject.class.getClassLoader());
  }
}
