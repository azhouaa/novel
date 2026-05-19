<template>
  <div class="header">
    <div class="mainNav" id="mainNav">
      <div class="box_center cf admin-bar">
        <div class="admin-title">管理后台</div>
        <div class="admin-welcome">您好，管理员</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AdminHeader",
};
</script>

<style scoped>
.admin-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  line-height: 44px;
  color: #fff;
}

.admin-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  margin-left: 120px;
}

.admin-welcome {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}
</style>
