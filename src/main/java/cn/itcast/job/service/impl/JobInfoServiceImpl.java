package cn.itcast.job.service.impl;

import cn.itcast.job.dao.JobInfoDao;
import cn.itcast.job.pojo.JobInfo;
import cn.itcast.job.service.JobInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class JobInfoServiceImpl implements JobInfoService {
    @Autowired
    private JobInfoDao jobInfoDao;

    @Override
    @Transactional
    public void save(JobInfo jobInfo) {
        //根据URL和发布时间查询数据
        JobInfo param = new JobInfo();
        param.setUrl(jobInfo.getUrl());
        param.setTime(jobInfo.getTime());
        List<JobInfo> list = this.findJobInfo(param);
        //判断查询结果
        if (list.size() == 0){
            this.jobInfoDao.saveAndFlush(jobInfo);
        }
    }

    @Override
    public List<JobInfo> findJobInfo(JobInfo jobInfo) {
        Example example = Example.of(jobInfo);
        List<JobInfo> list = this.jobInfoDao.findAll(example);
        return list;
    }

    @Override
    public Page<JobInfo> findJobInfoByPage(int page, int rows) {
        return this.jobInfoDao.findAll(PageRequest.of(page,rows));
    }
}
