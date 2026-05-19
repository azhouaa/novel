<template>
  <AdminHeader />
  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <div class="my_l">
        <ul class="log_list">
          <li><router-link class="link_4" :to="{ name: 'adminDashboard' }">大屏总览</router-link></li>
          <li><router-link class="link_4 on" :to="{ name: 'adminUserManage' }">用户管理</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'adminBookManage' }">书籍管理</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'adminCommentManage' }">评论管理</router-link></li>
        </ul>
      </div>
      <div class="my_r">
        <div class="setup-panel">
          <div class="setup-header">
            <h2>用户管理</h2>
            <p>可查看账号、审批作家申请、封禁账号、撤销作家权限。</p>
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
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  getUserinfo,
  adminListUsers,
  adminApproveAuthor,
  adminRevokeAuthor,
  adminBanUser,
  adminUnbanUser,
} from "@/api/user";
import AdminHeader from "@/components/admin/Header.vue";
import "@/assets/styles/backend-panel.css";

export default {
  name: "adminUserManage",
  components: { AdminHeader },
  setup() {
    const router = useRouter();
    const state = reactive({
      records: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    });

    onMounted(async () => {
      await checkAdmin();
      await load();
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

    return { ...toRefs(state), handleCurrentChange, approve, revoke, ban, unban };
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
.table-wrap { overflow-x: auto; }
.admin-table { width: 100%; border-collapse: collapse; }
.admin-table th,.admin-table td { border: 1px solid #e4ecf6; padding: 10px 8px; text-align: center; color: #4c607e; }
.admin-table th { background: #f3f8ff; color: #2a3f5d; }
.actions a { margin: 0 4px; color: #2b8be6; }
.tag-ok { color: #259f5c; font-weight: 600; }
.tag-bad { color: #d9534f; font-weight: 600; }
.tag-warn { color: #c58a00; font-weight: 600; }
</style>


