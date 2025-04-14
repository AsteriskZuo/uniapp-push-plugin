<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="text-area">
			<text class="title">{{title}}</text>
			<button @click="initPush">initPush</button>
			<button @click="onRegister">onRegister</button>
		</view>
	</view>
</template>

<script>
	// #ifdef APP-PLUS
	const EMPushUniPlugin = uni.requireNativePlugin("EMPushUniPlugin");
	export default {
		data() {
			return {
				title: 'Hello'
			}
		},
		onLoad() {

		},
		methods: {
			initPush: () => {
				console.log('initPushModule')
				EMPushUniPlugin.initPushModule();
			},
			onRegister: () => {
				console.log('onRegister')
				EMPushUniPlugin.onRegister((res)=>{
					console.log(res, 'onRegister res')
				});
			}
		}
	}
</script>

<style>
	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.logo {
		height: 200rpx;
		width: 200rpx;
		margin-top: 200rpx;
		margin-left: auto;
		margin-right: auto;
		margin-bottom: 50rpx;
	}

	.text-area {
		display: flex;
		justify-content: center;
	}

	.title {
		font-size: 36rpx;
		color: #8f8f94;
	}
</style>