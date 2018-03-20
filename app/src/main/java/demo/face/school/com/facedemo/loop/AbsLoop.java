package demo.face.school.com.facedemo.loop;

/**
 * 自定义人脸识别线程
 */
public abstract class AbsLoop extends Thread {
    public volatile Thread mBlinker = this;
    public boolean isStart = true;

    abstract public void setup();

    abstract public void loop();

    abstract public void over();

//	private Handler handler = new Handler(new Handler.Callback() {
//		@Override
//		public boolean handleMessage(Message message) {
//
//			if (message.what == 0){
//				loop();
//			}
//
//
//			return false;
//		}
//	});


    @Override
    public void run() {
//		Thread thisThread = Thread.currentThread();
//		setup();
//		while (mBlinker == thisThread) {
//				loop();
//		}
//		over();
    }

    public void break_loop() {
        mBlinker = null;
    }

    public void shutdown() {
        break_loop();
        try {
            if (this != Thread.currentThread()) {
                synchronized (this) {
                    this.notifyAll();
                }
                this.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}