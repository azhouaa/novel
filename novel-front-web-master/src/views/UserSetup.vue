<template>
  <Header />

  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <UserMenu />
      <div class="my_r">
        <div class="setup-panel">
          <div class="setup-header">
            <h2>账号设置</h2>
            <p>维护头像与阅读偏好，推荐内容会按你的选择动态调整。</p>
          </div>

          <div class="profile-card">
            <div class="avatar-wrap">
              <el-upload
                class="avatar-uploader"
                :action="baseUrl + '/front/resource/image'"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img :src="userPhoto ? imgBaseUrl + userPhoto : man" class="avatar" alt="用户头像" />
              </el-upload>
              <p class="avatar-tip">支持 JPG/PNG/WEBP，大小不超过 5MB</p>
            </div>

            <div class="profile-meta">
              <div class="meta-item">
                <span class="meta-label">昵称</span>
                <strong class="meta-value">{{ nickName || '未设置昵称' }}</strong>
              </div>
              <div class="meta-item">
                <span class="meta-label">已选偏好</span>
                <strong class="meta-value">{{ activeTags.length }} 项</strong>
              </div>
              <div class="meta-item hint">选中越精准，书城推荐越贴近你的口味。</div>
            </div>
          </div>

          <div class="prefer-card">
            <div class="prefer-head">
              <h3>阅读偏好</h3>
              <a href="javascript:void(0)" class="action-link" @click="resetPreferTags">重置</a>
            </div>

            <div class="prefer-tags-panel">
              <a
                v-for="tag in preferOptions"
                :key="tag"
                href="javascript:void(0)"
                :class="['prefer-tag-item', { on: isTagActive(tag) }]"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </a>
            </div>

            <div class="prefer-footer">
              <a href="javascript:void(0)" class="save-prefer-btn" @click="savePreferTags">
                {{ saveLoading ? '保存中...' : '保存偏好' }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/user.css";
import man from "@/assets/images/man.png";
import { reactive, toRefs, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { listCategorys } from "@/api/book";
import { getUserinfo, updateUserInfo } from "@/api/user";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import UserMenu from "@/components/user/Menu";

export default {
  name: "userSetup",
  components: {
    Header,
    Footer,
    UserMenu,
  },
  setup() {
    const state = reactive({
      userPhoto: "",
      nickName: "",
      preferTags: "",
      preferOptions: [],
      saveLoading: false,
      baseUrl: process.env.VUE_APP_BASE_API_URL,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    const activeTags = computed(() => parsePreferTags());

    onMounted(async () => {
      await Promise.all([loadUserInfo(), loadPreferOptions()]);
    });

    /**
     * 拉取用户基础信息，用于账号设置首屏展示。
     */
    const loadUserInfo = async () => {
      const { data } = await getUserinfo();
      state.userPhoto = data.userPhoto;
      state.nickName = data.nickName;
      state.preferTags = data.preferTags || "";
    };

    /**
     * 拉取男女频分类并构建偏好标签列表。
     */
    const loadPreferOptions = async () => {
      const [maleResp, femaleResp] = await Promise.all([
        listCategorys({ workDirection: 0 }),
        listCategorys({ workDirection: 1 }),
      ]);
      const tags = ["男频"]
        .concat((maleResp.data || []).map((item) => item.name))
        .concat((femaleResp.data || []).map((item) => (item.name === "女生频道" ? "女频" : item.name)));
      state.preferOptions = [...new Set(tags.filter(Boolean))];
    };

    /**
     * 将逗号分隔偏好字符串转换为数组，便于渲染与交互。
     */
    const parsePreferTags = () => {
      if (!state.preferTags) {
        return [];
      }
      return state.preferTags
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean);
    };

    const isTagActive = (tag) => parsePreferTags().includes(tag);

    /**
     * 点击标签时做选中/取消选中切换。
     */
    const toggleTag = (tag) => {
      const tags = parsePreferTags();
      const idx = tags.indexOf(tag);
      if (idx >= 0) {
        tags.splice(idx, 1);
      } else {
        tags.push(tag);
      }
      state.preferTags = tags.join(",");
    };

    /**
     * 保存偏好到服务端，供首页推荐与个性化场景使用。
     */
    const savePreferTags = async () => {
      if (state.saveLoading) {
        return;
      }
      state.saveLoading = true;
      try {
        await updateUserInfo({ preferTags: state.preferTags });
        ElMessage.success("阅读偏好已保存");
      } finally {
        state.saveLoading = false;
      }
    };

    const resetPreferTags = () => {
      state.preferTags = "";
    };

    /**
     * 头像上传前校验格式和体积，避免无效请求。
     */
    const beforeAvatarUpload = (rawFile) => {
      const allowTypes = ["image/jpeg", "image/png", "image/webp"];
      if (!allowTypes.includes(rawFile.type)) {
        ElMessage.error("请上传 JPG/PNG/WEBP 格式图片");
        return false;
      }
      if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error("图片大小不能超过 5MB");
        return false;
      }
      return true;
    };

    /**
     * 上传成功后同步更新头像，确保个人资料一致。
     */
    const handleAvatarSuccess = async (response) => {
      state.userPhoto = response.data;
      await updateUserInfo({ userPhoto: state.userPhoto });
      ElMessage.success("头像已更新");
    };

    return {
      ...toRefs(state),
      activeTags,
      man,
      beforeAvatarUpload,
      handleAvatarSuccess,
      isTagActive,
      toggleTag,
      savePreferTags,
      resetPreferTags,
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

.profile-card {
  display: flex;
  gap: 22px;
  padding: 20px;
  border: 1px solid #e4ecf6;
  border-radius: 10px;
  background: #fff;
  margin-bottom: 16px;
}

.avatar-wrap {
  width: 190px;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  border-radius: 12px;
  border: 1px solid #e1eaf6;
  display: block;
}

.avatar-tip {
  margin-top: 10px;
  color: #7f8ea5;
  font-size: 12px;
  line-height: 1.6;
}

.profile-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.meta-label {
  width: 80px;
  color: #8493a8;
}

.meta-value {
  color: #22344f;
  font-size: 18px;
}

.meta-item.hint {
  color: #5f7491;
  line-height: 1.7;
}

.prefer-card {
  border: 1px solid #e4ecf6;
  border-radius: 10px;
  background: #fff;
  padding: 20px;
}

.prefer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.prefer-head h3 {
  font-size: 18px;
  color: #24364f;
}

.action-link {
  color: #6c84a5;
}

.prefer-tags-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.prefer-tag-item {
  display: inline-block;
  padding: 4px 12px;
  border: 1px solid #d6e2f1;
  border-radius: 16px;
  color: #5a6f8f;
  transition: all 0.2s ease;
}

.prefer-tag-item:hover {
  border-color: #8bb4de;
  color: #326ca1;
}

.prefer-tag-item.on {
  border-color: #2196f3;
  color: #2196f3;
  background: #edf6ff;
}

.prefer-footer {
  margin-top: 18px;
}

.save-prefer-btn {
  display: inline-block;
  padding: 8px 18px;
  border-radius: 20px;
  background: linear-gradient(120deg, #2b8be6, #4eb2ff);
  color: #fff;
}

@media (max-width: 960px) {
  .profile-card {
    flex-direction: column;
  }

  .avatar-wrap {
    width: 100%;
  }
}
</style>
