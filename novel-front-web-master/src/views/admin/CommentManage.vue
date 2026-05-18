<template>
  <AdminHeader />
  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <div class="my_l">
        <ul class="log_list">
          <li><router-link class="link_4" :to="{ name: 'adminUserManage' }">用户管理</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'adminBookManage' }">书籍管理</router-link></li>
          <li><router-link class="link_4 on" :to="{ name: 'adminCommentManage' }">评论管理</router-link></li>
        </ul>
      </div>
      <div class="my_r">
        <div class="setup-panel">
          <div class="setup-header">
            <h2>评论管理</h2>
            <p>查看全站书评，支持删除不合规评论。</p>
          </div>
          <table cellpadding="0" cellspacing="0" class="admin-table">
            <thead><tr><th>评论ID</th><th>书名</th><th>用户</th><th>评论内容</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="item in records" :key="item.commentId">
                <td>{{ item.commentId }}</td>
                <td>{{ item.bookName || ("ID:" + item.bookId) }}</td>
                <td>{{ item.username || ("UID:" + item.userId) }}</td>
                <td class="txt">{{ item.commentContent }}</td>
                <td>{{ item.auditStatus }}</td>
                <td>{{ item.createTime }}</td>
                <td><a href="javascript:void(0)" @click="removeComment(item.commentId)">删除</a></td>
              </tr>
            </tbody>
          </table>
          <el-pagination small layout="prev, pager, next" :background="true" :page-size="pageSize" :total="total" class="mt-4" @current-change="handleCurrentChange" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { adminListComments, adminDeleteComment } from "@/api/user";
import AdminHeader from "@/components/admin/Header.vue";

export default {
  name: "adminCommentManage",
  components: { AdminHeader },
  setup() {
    const state = reactive({ records: [], total: 0, pageNum: 1, pageSize: 10 });
    onMounted(() => load());
    const load = async () => {
      const { data } = await adminListComments({ pageNum: state.pageNum, pageSize: state.pageSize });
      state.records = data.list || [];
      state.total = Number(data.total || 0);
      state.pageNum = data.pageNum;
      state.pageSize = data.pageSize;
    };
    const handleCurrentChange = (pageNum) => { state.pageNum = pageNum; load(); };
    const removeComment = async (commentId) => {
      await ElMessageBox.confirm(`确认删除评论ID=${commentId} 吗？`, "删除确认", { type: "warning" });
      await adminDeleteComment(commentId);
      ElMessage.success("评论已删除");
      load();
    };
    return { ...toRefs(state), handleCurrentChange, removeComment };
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
.admin-table th,.admin-table td { border: 1px solid #e4ecf6; padding: 10px 8px; text-align: center; color: #4c607e; }
.admin-table th { background: #f3f8ff; color: #2a3f5d; }
.txt { text-align: left; max-width: 420px; }
</style>
