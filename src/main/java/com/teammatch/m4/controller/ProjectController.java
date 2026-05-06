package com.teammatch.m4.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teammatch.m4.dto.ProjectCreateDTO;
import com.teammatch.m4.entity.Project;
import com.teammatch.m4.entity.TeamMember;
import com.teammatch.m4.service.ProjectService;
import com.teammatch.m4.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/m4/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;

    /** T-113 创建项目 */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectCreateDTO dto) {
        Project project = projectService.createProject(dto);
        return ResponseEntity.ok(project);
    }

    /** T-114 项目列表（分页 + 状态筛选） */
    @GetMapping
    public ResponseEntity<?> getProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        Page<Project> page = projectService.page(new Page<>(pageNum, pageSize), wrapper);
        return ResponseEntity.ok(page);
    }

    /** T-115 项目详情 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectInfo(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return ResponseEntity.badRequest().body("项目不存在");
        }
        return ResponseEntity.ok(project);
    }

    /** T-116 更新项目信息 */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProjectInfo(@PathVariable Long id, @RequestBody Project updateData) {
        updateData.setId(id);
        boolean success = projectService.updateById(updateData);
        return success ? ResponseEntity.ok("更新成功") : ResponseEntity.badRequest().body("更新失败");
    }

    /** T-117 开始项目 (recruiting -> in_progress) */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startProject(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            projectService.startProject(id, operatorId);
            return ResponseEntity.ok("已成功进入组队中阶段");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-118 结束项目 (in_progress -> ended) */
    @PostMapping("/{id}/end")
    public ResponseEntity<?> endProject(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            projectService.endProject(id, operatorId);
            return ResponseEntity.ok("项目已结束，互评窗口已开启");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-119 互评窗口懒检查 */
    @GetMapping("/{id}/eval-status")
    public ResponseEntity<?> checkEvalStatus(@PathVariable Long id) {
        try {
            String status = projectService.checkAndCloseEvalWindow(id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-132 项目成员列表 */
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(teamMemberService.getProjectMembers(id));
    }
}

