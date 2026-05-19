<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <div class="my_l">
        <ul class="log_list">
          <li><router-link class="link_4" :to="{ name: 'authorBookList' }">作家专区</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'authorBookUpload' }">上传小说</router-link></li>
          <li><router-link class="link_4" :to="{ name: 'authorUploadRecords' }">上传记录</router-link></li>
          <li><router-link class="link_4 on" :to="{ name: 'authorCommentManage' }">书评管理</router-link></li>
        </ul>
      </div>
      <div class="my_r">
        <div class="my_bookshelf">
          <div class="title cf"><h2 class="fl">我的书评</h2></div>
          <div class="no-data" v-if="total === 0">暂无书评数据。</div>
          <div class="updateTable" v-else>
            <table cellpadding="0" cellspacing="0">
              <thead><tr><th class="goread">评论ID</th><th class="goread">书名</th><th class="goread">用户</th><th class="goread">评论内容</th><th class="goread">状态</th><th class="goread">时间</th><th class="goread">操作</th></tr></thead>
              <tbody>
                <tr v-for="item in records" :key="item.commentId">
                  <td class="goread">{{ item.commentId }}</td>
                  <td class="goread">{{ item.bookName }}</td>
                  <td class="goread">{{ item.username || ("UID:" + item.userId) }}</td>
                  <td class="goread comment">{{ item.commentContent }}</td>
                  <td class="goread">{{ item.auditStatus }}</td>
                  <td class="goread">{{ item.createTime }}</td>
                  <td class="goread"><a href="javascript:void(0)" @click="removeComment(item.commentId)">删除</a></td>
                </tr>
              </tbody>
            </table>
            <el-pagination small layout="prev, pager, next" :background="true" :page-size="pageSize" :total="total" class="mt-4" @current-change="handleCurrentChange" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import "@/assets/styles/backend-panel.css";
import { reactive, toRefs, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { listAuthorComments } from "@/api/author";
import { deleteComment } from "@/api/user";
import AuthorHeader from "@/components/author/Header.vue";

export default {
  name: "authorCommentManage",
  components: { AuthorHeader },
  setup() {
    const state = reactive({ records: [], pageNum: 1, pageSize: 10, total: 0 });
    onMounted(() => load());
    const load = async () => {
      const { data } = await listAuthorComments({ pageNum: state.pageNum, pageSize: state.pageSize });
      state.records = data.list || [];
      state.total = Number(data.total || 0);
      state.pageNum = data.pageNum;
      state.pageSize = data.pageSize;
    };
    const handleCurrentChange = (pageNum) => { state.pageNum = pageNum; load(); };
    const removeComment = async (commentId) => {
      await ElMessageBox.confirm(`确认删除评论ID=${commentId} 吗？`, "删除确认", { type: "warning" });
      await deleteComment(commentId);
      ElMessage.success("评论已删除");
      load();
    };
    return { ...toRefs(state), handleCurrentChange, removeComment };
  },
};
</script>

<style scoped>
.userBox {
  margin: 0 auto 50px;
  background: #fff;
  border-radius: 6px;
}

.my_l {
  width: 198px;
  float: left;
  font-size: 13px;
  padding-top: 20px;
}

.my_l li a {
  display: block;
  height: 42px;
  line-height: 42px;
  padding-left: 62px;
  border-left: 4px solid #fff;
  margin-bottom: 5px;
  color: #666;
}

.my_l li .on {
  background-color: #fafafa;
  border-left: 2px solid #2196f3;
  color: #000;
  border-radius: 0 2px 2px 0;
}

.my_l .link_4 {
  background-position: 32px -314px;
}

.my_r {
  width: 739px;
  padding: 0 30px 30px;
  float: right;
  border-left: 1px solid #efefef;
  min-height: 470px;
}

.my_bookshelf .title {
  padding: 20px 0 15px;
  line-height: 30px;
}

.my_bookshelf h2 {
  font-size: 18px;
  font-weight: normal;
}

.updateTable {
  width: 739px;
  color: #999;
  border-top: 1px solid #f1f1f1;
}

.updateTable table {
  width: 100%;
  margin-bottom: 14px;
}

.updateTable th,
.updateTable td {
  height: 40px;
  line-height: 40px;
  vertical-align: middle;
  padding-left: 6px;
  font-weight: normal;
  text-align: left;
}

.updateTable th {
  background: #f9f9f9;
  color: #333;
  border-top: 1px solid #eee;
}

.updateTable .goread {
  text-align: center;
}

.updateTable tr:nth-child(2n) td {
  background: #fafafa;
}

.comment {
  text-align: left;
  max-width: 360px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-data {
  border-top: 1px solid #f1f1f1;
  color: #999;
  text-align: center;
  padding: 80px 0 70px;
  font-size: 14px;
}
</style>





