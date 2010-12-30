package no.magott.nicolas.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

public class MockUpCrawler {

	@Autowired
	private TaskExecutor taskExecutor;

	private final List<String> urls = Arrays.asList("http://www.mysite.com",
			"http://www.yoursite.com");

	public void doCrawl() throws InterruptedException, ExecutionException {
		List<FutureTask<Boolean>> tasks = new ArrayList<FutureTask<Boolean>>();

		// Runs crawling tasks
		for (String url : urls) {
			tasks.add(doCrawl(url));
		}

		// Checks status of crawling
		int count = 0;
		for (FutureTask<Boolean> futureTask : tasks) {
			
			try {
				//This code is executed by the "main thread", which means the timeout is determined by the client thread, not by each task
				//In other words, each task will have a timeout of (1 second * count+1)
				Boolean status = futureTask.get(1, TimeUnit.SECONDS);
				System.out.println("Crawling of "+ urls.get(count)+ " finished with status " +status);
			} catch (TimeoutException e) {
				System.err.println("Crawling of "+urls.get(count) +" timed out");
				futureTask.cancel(true);
			}finally{
				count++;
			}
		}

	}

	private FutureTask<Boolean> doCrawl(String url) {
		FutureTask<Boolean> crawlingTask = createFuture();
		//Execution is delegated to TaskExecutor (typically different threads)
		taskExecutor.execute(crawlingTask);
		return crawlingTask;

	}

	private FutureTask<Boolean> createFuture() {
		final FutureTask<Boolean> task = new FutureTask<Boolean>(
				new Callable<Boolean>() {
					//This code is executed in separate threads created by the TaskExecutor
					public Boolean call() throws Exception {
						// Your crawling magic goes here:
						System.out.println("Crawling something");
						consumeTime();
						return true;
					}
				});
		return task;
	}

	public void crawl() throws InterruptedException, ExecutionException {
		ConfigurableApplicationContext context = null;
		context = new ClassPathXmlApplicationContext("crawler-context.xml");
		context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

		doCrawl();
		context.close();
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException {
		MockUpCrawler crawler = new MockUpCrawler();
		crawler.crawl();
	}
	
	/**
	 * simulates time consuming work
	 * @throws InterruptedException in case calling thread is interrupted due to task being canceled
	 */
	private void consumeTime() throws InterruptedException{
		double temp = 0;
        for (int i = 0; i < 100 ; i++) {
            Thread.sleep(100); //Do not catch InterruptedException, it is what enables task cancelaton
            temp = Math.cos(Math.random());
        }
        System.out.println(temp);
	}

}
