package com.teammatch.m6.controller;

import com.teammatch.common.Result;
import com.teammatch.m6.dto.BoardCreateDTO;
import com.teammatch.m6.dto.BoardUpdateDTO;
import com.teammatch.m6.entity.Board;
import com.teammatch.m6.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 板块管理 Controller
 *
 * 根据详细设计文档 7.2 节定义
 * 管理端接口前缀：/api/admin
 * TODO: 管理端接口需要添加admin鉴权，等M3完成后接入
 */
@RestController
@RequestMapping("/admin/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 创建板块
     * POST /api/admin/boards
     *
     * @param dto 创建请求
     * @return 创建的板块
     */
    @PostMapping
    public Result<Board> createBoard(@Valid @RequestBody BoardCreateDTO dto) {
        try {
            Board board = boardService.createBoard(dto);
            return Result.success(board);
        } catch (IllegalArgumentException e) {
            // 名称重复、名称为空等参数错误
            return Result.fail(com.teammatch.common.ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 更新板块
     * PUT /api/admin/boards/{id}
     *
     * @param id 板块ID
     * @param dto 更新请求
     * @return 更新后的板块
     */
    @PutMapping("/{id}")
    public Result<Board> updateBoard(@PathVariable Long id, @Valid @RequestBody BoardUpdateDTO dto) {
        try {
            Board board = boardService.updateBoard(id, dto);
            return Result.success(board);
        } catch (IllegalArgumentException e) {
            // 区分"板块不存在"和"参数错误"
            String msg = e.getMessage();
            if (msg != null && msg.contains("板块不存在")) {
                return Result.fail(com.teammatch.common.ReasonCode.NOT_FOUND);
            }
            // 名称重复、名称为空、无效状态等参数错误
            return Result.fail(com.teammatch.common.ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 删除板块
     * DELETE /api/admin/boards/{id}
     * 仅允许无项目引用时删除
     *
     * @param id 板块ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBoard(@PathVariable Long id) {
        try {
            boardService.deleteBoard(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            // 板块不存在
            return Result.fail(com.teammatch.common.ReasonCode.NOT_FOUND);
        } catch (IllegalStateException e) {
            // 有项目引用，无法删除
            return Result.fail(com.teammatch.common.ReasonCode.STATUS_CONFLICT);
        }
    }

    /**
     * 获取板块详情
     * GET /api/admin/boards/{id}
     *
     * @param id 板块ID
     * @return 板块详情
     */
    @GetMapping("/{id}")
    public Result<Board> getBoard(@PathVariable Long id) {
        Board board = boardService.getBoardById(id);
        if (board == null) {
            return Result.fail(com.teammatch.common.ReasonCode.NOT_FOUND);
        }
        return Result.success(board);
    }

    /**
     * 获取所有板块列表（管理端）
     * GET /api/admin/boards
     *
     * @return 板块列表
     */
    @GetMapping
    public Result<List<Board>> listBoards() {
        List<Board> boards = boardService.list();
        return Result.success(boards);
    }
}
