package cn.itcast.job.task;

import cn.itcast.job.pojo.JobInfo;
import cn.itcast.job.selenium.SeleniumDownloader;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobProcessor implements PageProcessor {
    private String url = "https://search.51job.com/list/000000,000000,0000,01%252c32,9,99," +
            "Java,2,1.html?lang=c&postchannel=0000&workyear=99&cotype=99&degreefrom=99" +
            "&jobterm=99&companysize=99&ord_field=0&dibiaoid=0&line=&welfare=";

    @Override
    public void process(Page page) {
        //获取页面招聘列表数据
        List<Selectable> nodes = page.getHtml().$("div.j_joblist div.e").nodes();
        if (nodes.isEmpty()){
            String html = page.getHtml().toString();
            this.saveJobInfo(page);
        }else {
            //如果不为空，表示这是列表页，解析出详情页的URL地址，放在任务队列中
            for (Selectable node : nodes){
                //获取招聘信息详情页url
                String jobUrl = node.links().toString();
                //把获取到URL放入任务队列中
                page.addTargetRequest(jobUrl);
            }

            //解析下一页的URL
            if (url.equals(page.getUrl().toString())){
                String nextUrlStr = page.getUrl().toString();
                List<String> nextPageUrl = this.getNextPageUrl(nextUrlStr);
                System.out.println("下一页："+nextPageUrl);
                if (nextPageUrl.size()>0){
                    page.addTargetRequests(nextPageUrl);
                }
            }
        }
    }

    private void saveJobInfo(Page page) {
        JobInfo jobInfo = new JobInfo();
        Html html = page.getHtml();
        jobInfo.setCompanyName(html.css("div.cn p.cname a","text").toString());
        jobInfo.setCompanyAddr(Jsoup.parse(html.$("div.bmsg").nodes().get(1).$("p.fp").toString()).text());
        jobInfo.setCompanyInfo(html.css("div.tmsg","text").toString());
        jobInfo.setJobName(html.css("div.cn h1","text").toString());
        String msg = html.css("div.cn p.msg","text").toString();
        Integer index = msg.indexOf(" ");
        if (index == -1){
            index = msg.indexOf(" ");
        }
        jobInfo.setJobAddr(msg.substring(0, index)); //
        jobInfo.setJobInfo(Jsoup.parse(html.css("div.job_msg").toString()).text());  //
        jobInfo.setUrl(page.getUrl().toString());
        String salaryStr = html.$("div.cn strong", "text").toString();
        Integer[] salary = MathSalary.getSalary(salaryStr);
        jobInfo.setSalaryMin(salary[0]);
        jobInfo.setSalaryMax(salary[1]);
        int i = msg.lastIndexOf("-");
        String time = msg.substring(i - 2, i + 3);
        jobInfo.setTime(time);
        //将结果保存起来
        page.putField("jobInfo",jobInfo);
    }

    private List<String> getNextPageUrl(String nextPageUrl) {
        List<String> urlList = new ArrayList<>();
        int indexOf = nextPageUrl.indexOf(".html?");
        String preString = url.substring(0, indexOf);
        String sufStr = url.substring(indexOf);
        int i = preString.lastIndexOf(",")+1;
        String preString1 = preString.substring(i);
        String preString2 = preString.substring(0, i);
        Integer integer = Integer.parseInt(preString1);
        if (integer >=100){
            return null;
        }
//        integer = integer + 1 ;
//        preString2 += integer;
        for (int j = 2;j<=100;j++){
            String nextPage = preString2 + j;
            urlList.add(nextPage+sufStr);
        }
        return urlList;
    }

    private Site site = Site.me()
            .setCharset("gbk")
            .setTimeOut(10*1000)
            .setRetrySleepTime(3000)
            .setRetryTimes(3);
    @Override
    public Site getSite() {
        return site;
    }

    @Autowired
    private SpringDataPipeline springDataPipeline;
    //initialDelay当任务启动后，等等多久执行方法
    //fixedDelay每隔多久执行方法
    //@Scheduled(initialDelay = 1000,fixedDelay = 1000*100)
    public void process(){
        System.setProperty("selenuim_config", "E:/IdeaWed/crawler/itcast-crawler-testmoni/src/main/resources/config.ini");
        Spider.create(new JobProcessor())
        .addUrl(url)
        .thread(10)
        .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(100000)))
        .setDownloader(new SeleniumDownloader("D:\\chrome\\chromedriver.exe").setSleepTime(1000))
        .addPipeline(this.springDataPipeline)
        .run();
    }
}
