package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.mapper.CoachMapper;
import com.swimmingsys.model.dto.CoachAddDTO;
import com.swimmingsys.model.dto.CoachQueryDTO;
import com.swimmingsys.model.dto.CoachUpdateDTO;
import com.swimmingsys.model.entity.Coach;
import com.swimmingsys.model.vo.CoachVO;
import com.swimmingsys.service.CoachService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练服务实现类
 */
@Service
public class CoachServiceImpl implements CoachService {

    @Resource
    private CoachMapper coachMapper;

    /**
     * 获取教练列表（分页/条件查询）
     *
     * @param queryDTO 查询条件
     * @return 教练分页列表
     */
    @Override
    public IPage<CoachVO> getCoachList(CoachQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        
        // 姓名模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like("name", queryDTO.getName());
        }
        // 电话模糊查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like("phone", queryDTO.getPhone());
        }
        // 擅长项目模糊查询
        if (StringUtils.hasText(queryDTO.getSpecialty())) {
            queryWrapper.like("specialty", queryDTO.getSpecialty());
        }
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        // 排除已删除
        queryWrapper.eq("is_delete", 0);
        // 按创建时间倒序
        queryWrapper.orderByDesc("created_time");

        // 2. 执行分页查询
        Page<Coach> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Coach> coachPage = coachMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        return coachPage.convert(this::convertToCoachVO);
    }

    /**
     * 根据ID获取教练信息
     *
     * @param id 教练ID
     * @return 教练信息
     */
    @Override
    public CoachVO getCoachById(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("教练ID无效");
        }

        // 2. 查询教练
        Coach coach = coachMapper.selectById(id);
        if (coach == null || coach.getIsDelete() == 1) {
            throw new RuntimeException("教练不存在");
        }

        // 3. 转换并返回
        return convertToCoachVO(coach);
    }

    /**
     * 新增教练
     *
     * @param addDTO 新增教练信息
     * @return 新增的教练信息
     */
    @Override
    public CoachVO addCoach(CoachAddDTO addDTO) {
        // 1. 参数校验
        if (addDTO == null) {
            throw new RuntimeException("教练信息不能为空");
        }

        String phone = addDTO.getPhone().trim();

        // 2. 检查手机号是否已存在
        QueryWrapper<Coach> phoneQueryWrapper = new QueryWrapper<>();
        phoneQueryWrapper.eq("phone", phone);
        phoneQueryWrapper.eq("is_delete", 0);
        Long phoneCount = coachMapper.selectCount(phoneQueryWrapper);
        if (phoneCount > 0) {
            throw new RuntimeException("该手机号已被其他教练使用");
        }

        // 3. 创建教练对象
        Coach coach = new Coach();
        coach.setName(addDTO.getName().trim());
        coach.setPhone(phone);
        coach.setGender(addDTO.getGender());
        coach.setAvatar(addDTO.getAvatar());
        coach.setSpecialty(addDTO.getSpecialty().trim());
        coach.setDescription(addDTO.getDescription());
        coach.setStatus(addDTO.getStatus());

        // 4. 插入数据库
        int result = coachMapper.insert(coach);
        if (result <= 0) {
            throw new RuntimeException("新增教练失败");
        }

        // 5. 返回教练信息
        return convertToCoachVO(coach);
    }

    /**
     * 更新教练信息
     *
     * @param id        教练ID
     * @param updateDTO 更新信息
     * @return 更新后的教练信息
     */
    @Override
    public CoachVO updateCoach(Long id, CoachUpdateDTO updateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("教练ID无效");
        }
        if (updateDTO == null) {
            throw new RuntimeException("更新信息不能为空");
        }

        // 2. 查询教练是否存在
        Coach coach = coachMapper.selectById(id);
        if (coach == null || coach.getIsDelete() == 1) {
            throw new RuntimeException("教练不存在");
        }

        // 3. 更新字段
        if (StringUtils.hasText(updateDTO.getName())) {
            coach.setName(updateDTO.getName().trim());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            // 检查手机号是否已被其他教练使用
            QueryWrapper<Coach> phoneQueryWrapper = new QueryWrapper<>();
            phoneQueryWrapper.eq("phone", updateDTO.getPhone().trim());
            phoneQueryWrapper.ne("id", id);
            phoneQueryWrapper.eq("is_delete", 0);
            Long phoneCount = coachMapper.selectCount(phoneQueryWrapper);
            if (phoneCount > 0) {
                throw new RuntimeException("该手机号已被其他教练使用");
            }
            coach.setPhone(updateDTO.getPhone().trim());
        }
        if (updateDTO.getGender() != null) {
            coach.setGender(updateDTO.getGender());
        }
        if (updateDTO.getAvatar() != null) {
            coach.setAvatar(updateDTO.getAvatar());
        }
        if (StringUtils.hasText(updateDTO.getSpecialty())) {
            coach.setSpecialty(updateDTO.getSpecialty().trim());
        }
        if (updateDTO.getDescription() != null) {
            coach.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null) {
            coach.setStatus(updateDTO.getStatus());
        }

        // 4. 更新数据库
        int result = coachMapper.updateById(coach);
        if (result <= 0) {
            throw new RuntimeException("更新教练信息失败");
        }

        // 5. 返回更新后的教练信息
        return convertToCoachVO(coach);
    }

    /**
     * 删除教练（逻辑删除）
     *
     * @param id 教练ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteCoach(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("教练ID无效");
        }

        // 2. 查询教练是否存在
        Coach coach = coachMapper.selectById(id);
        if (coach == null || coach.getIsDelete() == 1) {
            throw new RuntimeException("教练不存在");
        }

        // 3. 逻辑删除（MyBatis-Plus会自动处理@TableLogic注解）
        int result = coachMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 获取在职教练列表（用于下拉选择）
     *
     * @return 在职教练列表
     */
    @Override
    public List<CoachVO> getActiveCoachList() {
        // 查询在职教练
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByAsc("name");

        List<Coach> coachList = coachMapper.selectList(queryWrapper);
        
        // 转换为VO列表
        return coachList.stream()
                .map(this::convertToCoachVO)
                .collect(Collectors.toList());
    }

    /**
     * 将Coach实体类转换为CoachVO
     *
     * @param coach 教练实体
     * @return 教练VO
     */
    private CoachVO convertToCoachVO(Coach coach) {
        if (coach == null) {
            return null;
        }
        CoachVO coachVO = new CoachVO();
        BeanUtils.copyProperties(coach, coachVO);
        return coachVO;
    }
}
