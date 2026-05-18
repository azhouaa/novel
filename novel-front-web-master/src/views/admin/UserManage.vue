<template>
  <Header />
  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <div class="my_r">
        <div class="setup-panel">
          <div class="setup-header">
            <h2>管理员用户管理</h2>
            <p>可查看账号、审批作家申请、封禁账号、撤销作家权限、删除小说。</p>
          </div>

          <div class="table-wrap">
            <table cellpadding="0" cellspacing="0" class="admin-table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>账号状态</th>
                  <th>作家状态</th>
                  <th>上传权限</th>
                  <th>注册时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in records" :key="item.userId">
                  <td>{{ item.userId }}</td>
                  <td>{{ item.username }}</td>
                  <td>{{ item.nickName || '-' }}</td>
                  <td>
                    <span :class="Number(item.userStatus) === 1 ? 'tag-bad' : 'tag-ok'">
                      {{ Number(item.userStatus) === 1 ? '封禁' : '正常' }}
                    </span>
                  </td>
                  <td>
                    <span v-if="Number(item.isAuthor) !== 1">普通用户</span>
                    <span v-else-if="Number(item.authorStatus) === 2" class="tag-warn">待审核</span>
                    <span v-else-if="Number(item.authorStatus) === 0" class="tag-ok">正常作家</span>
                    <span v-else class="tag-bad">已撤销</span>
                  </td>
                  <td>{{ Number(item.canUploadNovel) === 1 ? '已开通' : '未开通' }}</td>
                  <td>{{ item.createTime }}</td>
                  <td class="actions">
                    <a href="javascript:void(0)" @click="approve(item)">同意作家</a>
                    <a href="javascript:void(0)" @click="revoke(item)">撤销作家</a>
                    <a href="javascript:void(0)" @click="ban(item)">封禁账号</a>
                    <a href="javascript:void(0)" @click="unban(item)">解除封禁</a>
                  </td>
                </tr>
              </tbody>
            </table>

            <el-pagination
              small
              layout="prev, pager, next"
              :background="true"
              :page-size="pageSize"
              :total="total"
              class="mt-4"
              @current-change="handleCurrentChange"
            />
          </div>

          <div class="delete-book-panel">
            <h3>管理员删除小说</h3>
            <div class="delete-row">
              <input v-model="deleteBookName" type="text" class="s_input" placeholder="输入小说名称" />
              <a href="javascript:void(0)" class="btn-red" @click="deleteBookByName">删除小说</a>
            </div>
            <p class="tip">删除会连同章节与正文一起删除，仅管理员可操作；演示版按书名命中第一本。</p>
          </div>

          <div class="audit-panel">
            <h3>待审核书籍</h3>
            <table cellpadding="0" cellspacing="0" class="admin-table">
              <thead>
                <tr>
                  <th>书籍ID</th>
                  <th>书名</th>
                  <th>作者</th>
                  <th>更新时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in pendingBooks" :key="'book-' + item.bookId">
                  <td>{{ item.bookId }}</td>
                  <td>{{ item.bookName }}</td>
                  <td>{{ item.authorName }}</td>
                  <td>{{ item.updateTime }}</td>
                  <td class="actions">
                    <a href="javascript:void(0)" @click="auditBook(item.bookId, true)">通过</a>
                    <a href="javascript:void(0)" @click="auditBook(item.bookId, false)">驳回</a>
                  </td>
                </tr>
                <tr v-if="pendingBooks.length === 0">
                  <td colspan="5">暂无待审核书籍</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="audit-panel">
            <h3>待审核章节</h3>
            <table cellpadding="0" cellspacing="0" class="admin-table">
              <thead>
                <tr>
                  <th>章节ID</th>
                  <th>书籍ID</th>
                  <th>书名</th>
                  <th>章节名</th>
                  <th>更新时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in pendingChapters" :key="'chapter-' + item.chapterId">
                  <td>{{ item.chapterId }}</td>
                  <td>{{ item.bookId }}</td>
                  <td>{{ item.bookName || '-' }}</td>
                  <td>{{ item.chapterName }}</td>
                  <td>{{ item.updateTime }}</td>
                  <td class="actions">
                    <a href="javascript:void(0)" @click="previewChapter(item.chapterId)">查看</a>
                    <a href="javascript:void(0)" @click="auditChapter(item.chapterId, true)">通过</a>
                    <a href="javascript:void(0)" @click="auditChapter(item.chapterId, false)">驳回</a>
                  </td>
                </tr>
                <tr v-if="pendingChapters.length === 0">
                  <td colspan="6">暂无待审核章节</td>
                </tr>
              </tbody>
            </table>
          </div>

          <el-dialog v-model="chapterPreviewVisible" title="章节审核预览" width="65%">
            <div class="preview-box">
              <h4 class="preview-title">{{ chapterPreview.chapterName || '未命名章节' }}</h4>
              <pre class="preview-content">{{ chapterPreview.chapterContent }}</pre>
            </div>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getUserinfo,
  adminListUsers,
  adminApproveAuthor,
  adminRevokeAuthor,
  adminBanUser,
  adminUnbanUser,
  adminDeleteBookByName,
  adminListPendingBooks,
  adminListPendingChapters,
  adminAuditBook,
  adminAuditChapter,
  adminGetChapterDetail,
} from "@/api/user";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";

export default {
  name: "adminUserManage",
  components: {
    Header,
    Footer,
  },
  setup() {
    const router = useRouter();
    const state = reactive({
      records: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      deleteBookName: "",
      pendingBooks: [],
      pendingChapters: [],
      chapterPreviewVisible: false,
      chapterPreview: {
        chapterName: "",
        chapterContent: "",
      },
    });

    onMounted(async () => {
      await checkAdmin();
      await load();
      await loadPendingAudits();
    });

    /**
     * 校验管理员身份，防止普通用户访问后台。
     */
    const checkAdmin = async () => {
      const { data } = await getUserinfo();
      if (Number(data?.isAdmin) !== 1) {
        ElMessage.error("仅 root 管理员可访问该页面");
        router.push({ name: "home" });
      }
    };

    /**
     * 加载用户列表数据。
     */
    const load = async () => {
      const { data } = await adminListUsers({ pageNum: state.pageNum, pageSize: state.pageSize });
      state.records = data.list || [];
      state.total = Number(data.total || 0);
      state.pageNum = data.pageNum;
      state.pageSize = data.pageSize;
    };

    const handleCurrentChange = (pageNum) => {
      state.pageNum = pageNum;
      load();
    };

    const approve = async (item) => {
      await adminApproveAuthor(item.userId);
      ElMessage.success("已同意作家申请，并自动开通上传权限");
      load();
    };

    const revoke = async (item) => {
      await adminRevokeAuthor(item.userId);
      ElMessage.success("已撤销作家权限，并自动关闭上传权限");
      load();
    };

    const ban = async (item) => {
      await adminBanUser(item.userId);
      ElMessage.success("账号已封禁");
      load();
    };

    const unban = async (item) => {
      await adminUnbanUser(item.userId);
      ElMessage.success("账号已解除封禁");
      load();
    };

    const deleteBookByName = async () => {
      const bookName = (state.deleteBookName || "").trim();
      if (!bookName) {
        ElMessage.error("请先输入小说名称");
        return;
      }
      await ElMessageBox.confirm(`确认删除小说《${bookName}》吗？`, "删除确认", {
        confirmButtonText: "确认删除",
        cancelButtonText: "取消",
        type: "warning",
      });
      await adminDeleteBookByName(bookName);
      ElMessage.success("小说已删除");
      state.deleteBookName = "";
    };

    const loadPendingAudits = async () => {
      const [bookResp, chapterResp] = await Promise.all([
        adminListPendingBooks({ pageNum: 1, pageSize: 20 }),
        adminListPendingChapters({ pageNum: 1, pageSize: 20 }),
      ]);
      state.pendingBooks = bookResp.data?.list || [];
      state.pendingChapters = chapterResp.data?.list || [];
    };

    const auditBook = async (bookId, pass) => {
      await adminAuditBook(bookId, pass);
      ElMessage.success(pass ? "书籍审核已通过" : "书籍已驳回");
      await loadPendingAudits();
      await load();
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

    return {
      ...toRefs(state),
      handleCurrentChange,
      approve,
      revoke,
      ban,
      unban,
      deleteBookByName,
      auditBook,
      auditChapter,
      previewChapter,
    };
  },
};
</script>

<style scoped>
.user-center-wide {
  padding: 0 24px 26px;
}

.user-center-wide .my_r {
  width: 100%;
  float: none;
  border-left: 0;
  padding: 0;
  min-height: auto;
}

.setup-panel {
  border: 1px solid #e7edf5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  padding: 24px;
}

.setup-header {
  margin-bottom: 18px;
  border-bottom: 1px solid #e8eef7;
  padding-bottom: 14px;
}

.setup-header h2 {
  font-size: 24px;
  color: #1f2f46;
  margin-bottom: 8px;
}

.setup-header p {
  color: #7a8aa2;
  font-size: 14px;
}

.table-wrap {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  border: 1px solid #e4ecf6;
  padding: 10px 8px;
  text-align: center;
  color: #4c607e;
}

.admin-table th {
  background: #f3f8ff;
  color: #2a3f5d;
}

.actions a {
  margin: 0 4px;
  color: #2b8be6;
}

.tag-ok {
  color: #259f5c;
  font-weight: 600;
}

.tag-bad {
  color: #d9534f;
  font-weight: 600;
}

.tag-warn {
  color: #c58a00;
  font-weight: 600;
}

.delete-book-panel {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid #e4ecf6;
  border-radius: 8px;
  background: #fff;
}

.delete-book-panel h3 {
  margin-bottom: 12px;
  color: #24364f;
}

.delete-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.s_input {
  width: 220px;
  height: 36px;
  line-height: 36px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 10px;
}

.btn-red {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(120deg, #d84545, #f16b6b);
}

.tip {
  margin-top: 8px;
  color: #7a8aa2;
  font-size: 12px;
}

.audit-panel {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid #e4ecf6;
  border-radius: 8px;
  background: #fff;
}

.audit-panel h3 {
  margin-bottom: 12px;
  color: #24364f;
}

.preview-box {
  max-height: 60vh;
  overflow-y: auto;
}

.preview-title {
  margin-bottom: 10px;
  color: #24364f;
  font-size: 16px;
}

.preview-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #324760;
  background: #f8fbff;
  border: 1px solid #e4ecf6;
  border-radius: 8px;
  padding: 12px;
}
</style>
