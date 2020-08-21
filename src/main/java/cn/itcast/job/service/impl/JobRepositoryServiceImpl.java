package cn.itcast.job.service.impl;

import cn.itcast.job.dao.JobRepository;
import cn.itcast.job.pojo.JobInfoField;
import cn.itcast.job.pojo.JobResult;
import cn.itcast.job.service.JobRepositoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class JobRepositoryServiceImpl implements JobRepositoryService {

    @Autowired
    private JobRepository jobRepository;

    @Override
    public void save(JobInfoField jobInfoField) {
        this.jobRepository.save(jobInfoField);
    }

    @Override
    public void saveAll(List<JobInfoField> list) {
        this.jobRepository.saveAll(list);
    }
//    salary: 5-10
//    page: 1
//    jobaddr: 北京
//    keyword: Java
    /**
     * 根据条件分页查询招聘信息
     * @param salary
     * @param jobaddr
     * @param keyword
     * @param page
     * @return
     */
    @Override
    public JobResult search(String salary, String jobaddr, String keyword, Integer page) {
        int salaryMin = 0, salaryMax = 0;
        String[] salarys = salary.split("-");
        if ("*".equals(salarys[0])){
            salaryMin = 0;
        }else {
            salaryMin = Integer.parseInt(salarys[0])*10000;
        }
        if ("*".equals(salarys[1])){
            salaryMax = 900000000;
        }else {
            salaryMax = Integer.parseInt(salarys[1])*10000;
        }
        if (StringUtils.isBlank(jobaddr)){
            jobaddr="*";
        }
        if (StringUtils.isBlank(keyword)){
            keyword = "*";
        }
        Page<JobInfoField> pages =this.jobRepository.findBySalaryMinBetweenAndSalaryMaxBetweenAndJobAddrAndJobNameAndJobInfo(
                salaryMin,salaryMax,salaryMin,salaryMax,jobaddr,keyword,keyword, PageRequest.of(page-1,30));

        JobResult jobResult = new JobResult();
        jobResult.setRows(pages.getContent());
        jobResult.setPageTotal(pages.getTotalPages());
        return jobResult;
    }
}
