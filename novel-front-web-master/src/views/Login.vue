<template>
  <Header />

  <div class="main box_center cf">
    <div class="userBox cf">
      <div class="user_l">
        <form method="post" action="./login.html" id="form1">
          <h3>登录阿洲小说在线</h3>
          <ul class="log_list">
            <li><span id="LabErr"></span></li>
            <li>
              <input
              v-model="username"
                name="txtUName"
                type="text"
                id="txtUName"
                placeholder="手机号码"
                class="s_input icon_name"
              />
            </li>
            <li>
              <input
              v-model="password"
                name="txtPassword"
                type="password"
                id="txtPassword"
                placeholder="密码"
                class="s_input icon_key"
              />
            </li>
            <li>
              <input
                type="button"
                name="btnLogin"
                value="登录"
                id="btnLogin"
                class="btn_red"
                @click="loginUser"
              />
            </li>
          </ul>
        </form>
      </div>
      <div class="user_r">
        <p class="tit">还没有注册账号？</p>
        <router-link :to="{name: 'register'}" class="btn_ora_white">立即注册</router-link>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/user.css";
import { reactive, toRefs } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login, getUserinfo } from "@/api/user";
import { setToken, setNickName,setUid } from "@/utils/auth";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
export default {
  name: "loginPage",
  components: {
    Header,
    Footer,
  },
  setup() {
    const router = useRouter();

    const state = reactive({
      username: "",
      password: ""
    });

    /**
     * 登录并根据账号类型跳转页面。
     */
    const loginUser = async () => {
      if (!state.username) {
        ElMessage.error("用户名不能为空！");
        return;
      }
      if (!/^1[3|4|5|6|7|8|9][0-9]{9}/.test(state.username)) {
        ElMessage.error("手机号格式不正确！");
        return;
      }
      if (!state.password) {
        ElMessage.error("密码不能为空！");
        return;
      }

      const { data } = await login(state);
      setToken(data.token);
      setUid(data.uid);
      setNickName(data.nickName);

      // 登录后拉取用户信息，root 账号直接跳转管理界面。
      const userResp = await getUserinfo();
      if (Number(userResp.data?.isAdmin) === 1) {
        router.push({ name: "adminUserManage" });
        return;
      }
      router.push({ path: "/home" });
    };

    return {
      ...toRefs(state),
      loginUser,
    };
  },
};
</script>


<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.updateTable .style a {
  color: #999;
}
.updateTable .author a {
  color: #999;
  cursor: text;
}
.bind,
.updateTable .style a:hover {
  color: #f65167;
}
.userBox {
  margin: 0 auto 50px;
  background: #fff;
  border-radius: 6px;
}
.channelViewhistory .userBox {
  margin: 0 auto;
}
.user_l {
  width: 350px;
  float: left;
  padding: 100px 124px;
}
.user_l h3 {
  font-size: 23px;
  font-weight: normal;
  line-height: 1;
  text-align: center;
}
.user_l #LabErr {
  color: #ff4040;
  display: block;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 14px;
}
.user_l .log_list {
  width: 350px;
}
.user_l .s_input {
  margin-bottom: 25px;
  font-size: 14px;
}
.s_input {
  width: 348px;
  height: 38px;
  line-height: 38px\9;
  vertical-align: middle;
  border: 1px solid #ddd;
  border-radius: 2px;
}
.icon_name,
.icon_key,
.icon_code {
  width: 312px;
  padding-left: 36px;
}
</style>
