<template>
  <AdminHeader />
  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <div class="my_l">
        <ul class="log_list">
          <li><router-link class="link_4" :to="{ name: 'adminUserManage' }">用户管理</router-link></li>
          <li><router-link class="link_4 on" :to="{ name: 'adminBookManage' }">书籍管理</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'adminCommentManage' }">评论管理</router-link></li>
        </ul>
      </div>
      <div class="my_r">
        <div class="setup-panel">
          <div class="setup-header">
            <h2>书籍管理</h2>
            <p>负责书籍删除、书籍审核、章节审核。</p>
          </div>

          <div class="delete-book-panel">
            <h3>按书名删除小说</h3>
            <div class="delete-row">
              <input v-model="deleteBookName" type="text" class="s_input" placeholder="输入小说名称" />
              <a href="javascript:void(0)" class="btn-red" @click="deleteBookByName">删除小说</a>
            </div>
          </div>

          <div class="audit-panel">
            <h3>待审核书籍</h3>
            <table cellpadding="0" cellspacing="0" class="admin-table">
              <thead><tr><th>书籍ID</th><th>书名</th><th>作者</th><th>更新时间</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="item in pendingBooks" :key="'book-' + item.bookId">
                  <td>{{ item.bookId }}</td><td>{{ item.bookName }}</td><td>{{ item.authorName }}</td><td>{{ item.updateTime }}</td>
                  <td class="actions"><a href="javascript:void(0)" @click="auditBook(item.bookId,true)">通过</a><a href="javascript:void(0)" @click="auditBook(item.bookId,false)">驳回</a></td>
                </tr>
                <tr v-if="pendingBooks.length===0"><td colspan="5">暂无待审核书籍</td></tr>
              </tbody>
            </table>
          </div>

          <div class="audit-panel">
            <h3>待审核章节</h3>
            <table cellpadding="0" cellspacing="0" class="admin-table">
              <thead><tr><th>章节ID</th><th>书籍ID</th><th>书名</th><th>章节名</th><th>更新时间</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="item in pendingChapters" :key="'chapter-' + item.chapterId">
                  <td>{{ item.chapterId }}</td><td>{{ item.bookId }}</td><td>{{ item.bookName || '-' }}</td><td>{{ item.chapterName }}</td><td>{{ item.updateTime }}</td>
                  <td class="actions"><a href="javascript:void(0)" @click="previewChapter(item.chapterId)">查看</a><a href="javascript:void(0)" @click="auditChapter(item.chapterId,true)">通过</a><a href="javascript:void(0)" @click="auditChapter(item.chapterId,false)">驳回</a></td>
                </tr>
                <tr v-if="pendingChapters.length===0"><td colspan="6">暂无待审核章节</td></tr>
              </tbody>
            </table>
          </div>

          <el-dialog v-model="chapterPreviewVisible" title="章节审核预览" width="65%">
            <div class="preview-box">
              <h4 class="preview-title">{{ chapterPreview.chapterName || "未命名章节" }}</h4>
              <pre class="preview-content">{{ chapterPreview.chapterContent }}</pre>
            </div>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  adminDeleteBookByName,
  adminListPendingBooks,
  adminListPendingChapters,
  adminAuditBook,
  adminAuditChapter,
  adminGetChapterDetail,
} from "@/api/user";
import AdminHeader from "@/components/admin/Header.vue";

export default {
  name: "adminBookManage",
  components: { AdminHeader },
  setup() {
    const state = reactive({
      deleteBookName: "",
      pendingBooks: [],
      pendingChapters: [],
      chapterPreviewVisible: false,
      chapterPreview: { chapterName: "", chapterContent: "" },
    });

    onMounted(() => loadPendingAudits());

    const loadPendingAudits = async () => {
      const [bookResp, chapterResp] = await Promise.all([
        adminListPendingBooks({ pageNum: 1, pageSize: 20 }),
        adminListPendingChapters({ pageNum: 1, pageSize: 20 }),
      ]);
      state.pendingBooks = bookResp.data?.list || [];
      state.pendingChapters = chapterResp.data?.list || [];
    };

    const deleteBookByName = async () => {
      const bookName = (state.deleteBookName || "").trim();
      if (!bookName) return ElMessage.error("请先输入小说名称");
      await ElMessageBox.confirm(`确认删除小说《${bookName}》吗？`, "删除确认", { type: "warning" });
      await adminDeleteBookByName(bookName);
      ElMessage.success("小说已删除");
      state.deleteBookName = "";
      await loadPendingAudits();
    };

    const auditBook = async (bookId, pass) => {
      await adminAuditBook(bookId, pass);
      ElMessage.success(pass ? "书籍审核已通过" : "书籍已驳回");
      await loadPendingAudits();
    };

    const auditChapter = async (chapterId, pass) => {
      await adminAuditChapter(chapterId, pass);
      ElMessage.success(pass ? "章节审核已通过" : "章节已驳回");
      await loadPendingAudits();
    };

    const previewChapter = async (chapterId) => {
      const { data } = await adminGetChapterDetail(chapterId);
      state.chapterPreview.chapterName = data?.chapterName || "";
      state.chapterPreview.chapterContent = data?.chapterContent || "";
      state.chapterPreviewVisible = true;
    };

    return { ...toRefs(state), deleteBookByName, auditBook, auditChapter, previewChapter };
  },
};
</script>

<style scoped>
.user-center-wide { padding: 0 24px 26px; }
.my_l { width: 198px; float: left; font-size: 13px; padding-top: 20px; }
.my_l li a { display: block; height: 42px; line-height: 42px; padding-left: 62px; border-left: 4px solid #fff; margin-bottom: 5px; color: #666; }
.my_l li .on { background-color: #fafafa; border-left: 2px solid #2196f3; color: #000; border-radius: 0 2px 2px 0; }
.my_l .link_4 { background-position: 32px -314px; }
.user-center-wide .my_r { width: 739px; padding: 0 30px 30px; float: right; border-left: 1px solid #efefef; min-height: 470px; }
.setup-panel { border: 1px solid #e7edf5; border-radius: 10px; background: linear-gradient(180deg,#fff 0%,#f7fbff 100%); padding: 24px; }
.setup-header { margin-bottom: 18px; border-bottom: 1px solid #e8eef7; padding-bottom: 14px; }
.setup-header h2 { font-size: 24px; color: #1f2f46; margin-bottom: 8px; }
.setup-header p { color: #7a8aa2; font-size: 14px; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th, .admin-table td { border: 1px solid #e4ecf6; padding: 10px 8px; text-align: center; color: #4c607e; }
.admin-table th { background: #f3f8ff; color: #2a3f5d; }
.actions a { margin: 0 4px; color: #2b8be6; }
.delete-book-panel, .audit-panel { margin-top: 20px; padding: 16px; border: 1px solid #e4ecf6; border-radius: 8px; background: #fff; }
.delete-row { display: flex; gap: 10px; align-items: center; }
.s_input { width: 260px; height: 36px; line-height: 36px; border: 1px solid #ddd; border-radius: 4px; padding: 0 10px; }
.btn-red { display: inline-block; padding: 8px 16px; border-radius: 20px; color: #fff; background: linear-gradient(120deg,#d84545,#f16b6b); }
.preview-box { max-height: 60vh; overflow-y: auto; }
.preview-title { margin-bottom: 10px; color: #24364f; font-size: 16px; }
.preview-content { white-space: pre-wrap; line-height: 1.8; color: #324760; background: #f8fbff; border: 1px solid #e4ecf6; border-radius: 8px; padding: 12px; }
</style>
