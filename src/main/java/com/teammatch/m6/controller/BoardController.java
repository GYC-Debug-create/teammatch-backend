package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.dto.BoardCreateDTO;
import com.teammatch.m6.dto.BoardUpdateDTO;
import com.teammatch.m6.entity.Board;
import com.teammatch.m6.service.BoardService;
import com.teammatch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 板块管理 Controller
 *
 * 根据详细设计文档 7.2 节定义
 * 管理端接口前缀：/api/admin
 * 需要管理员权限（role=admin）
 */
@RestController
@RequestMapping("/admin/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final AuthService authService;

    /**
     * 创建板块
     * POST /api/admin/boards
     * 需要管理员权限
     *
     * @param dto 创建请求
     * @param token 认证token
     * @return 创建的板块
     */
    @PostMapping
    public Result<Board> createBoard(@Valid @RequestBody BoardCreateDTO dto,
                                     @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }
        try {
            Board board = boardService.createBoard(dto);
            return Result.success(board);
        } catch (IllegalArgumentException e) {
            // 名称重复、名称为空等参数错误
            return Result.fail(ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 更新板块
     * PUT /api/admin/boards/{id}
     * 需要管理员权限
     *
     * @param id 板块ID
     * @param dto 更新请求
     * @param token 认证token
     * @return 更新后的板块
     */
    @PutMapping("/{id}")
    public Result<Board> updateBoard(@PathVariable Long id,
                                     @Valid @RequestBody BoardUpdateDTO dto,
                                     @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }
        try {
            Board board = boardService.updateBoard(id, dto);
            return Result.success(board);
        } catch (IllegalArgumentException e) {
            // 区分"板块不存在"和"参数错误"
            String msg = e.getMessage();
            if (msg != null && msg.contains("板块不存在")) {
                return Result.fail(ReasonCode.NOT_FOUND);
            }
            // 名称重复、名称为空、无效状态等参数错误
            return Result.fail(ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 删除板块
     * DELETE /api/admin/boards/{id}
     * 仅允许无项目引用时删除
     * 需要管理员权限
     *
     * @param id 板块ID
     * @param token 认证token
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBoard(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }
        try {
            boardService.deleteBoard(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            // 板块不存在
            return Result.fail(ReasonCode.NOT_FOUND);
        } catch (IllegalStateException e) {
            // 有项目引用，无法删除
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }

    /**
     * 获取板块详情
     * GET /api/admin/boards/{id}
     * 需要管理员权限
     *
     * @param id 板块ID
     * @param token 认证token
     * @return 板块详情
     */
    @GetMapping("/{id}")
    public Result<Board> getBoard(@PathVariable Long id,
                                  @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }
        Board board = boardService.getBoardById(id);
        if (board == null) {
            return Result.fail(ReasonCode.NOT_FOUND);
        }
        return Result.success(board);
    }

    /**
     * 获取所有板块列表（管理端）
     * GET /api/admin/boards
     * 需要管理员权限
     *
     * @param token 认证token
     * @return 板块列表
     */
    @GetMapping
    public Result<List<Board>> listBoards(@RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }
        List<Board> boards = boardService.list();
        return Result.success(boards);
    }
}
