package com.tgm.tgmc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.feature.auth.presentation.ForgotPasswordScreen
import com.tgm.tgmc.feature.auth.presentation.LoginScreen
import com.tgm.tgmc.feature.auth.presentation.RegisterScreen
import com.tgm.tgmc.feature.auth.presentation.RoleSelectionScreen
import com.tgm.tgmc.feature.child.presentation.ChildConsentScreen
import com.tgm.tgmc.feature.child.presentation.ChildPairScreen
import com.tgm.tgmc.feature.parent.alerts.AlertsScreen
import com.tgm.tgmc.feature.parent.appblock.AppBlockScreen
import com.tgm.tgmc.feature.parent.audio.LiveAudioScreen
import com.tgm.tgmc.feature.parent.camera.RemoteCameraScreen
import com.tgm.tgmc.feature.parent.dashboard.ParentDashboardScreen
import com.tgm.tgmc.feature.parent.location.LocationScreen
import com.tgm.tgmc.feature.parent.mirror.ScreenMirrorScreen
import com.tgm.tgmc.feature.parent.pairing.PairingQrScreen
import com.tgm.tgmc.feature.parent.pairing.PairingStartScreen
import com.tgm.tgmc.feature.parent.schedule.ScheduleScreen
import com.tgm.tgmc.feature.parent.settings.SettingsScreen
import com.tgm.tgmc.feature.splash.SplashScreen
import com.tgm.tgmc.navigation.TgmcRoutes.AUTH_GRAPH
import com.tgm.tgmc.navigation.TgmcRoutes.CHILD_GRAPH
import com.tgm.tgmc.navigation.TgmcRoutes.PARENT_GRAPH

@Composable
fun TgmcNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TgmcRoutes.SPLASH
    ) {
        // ── Splash (role detection) ──────────────────────────────
        composable(TgmcRoutes.SPLASH) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(AUTH_GRAPH) {
                        popUpTo(TgmcRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToParent = {
                    navController.navigate(PARENT_GRAPH) {
                        popUpTo(TgmcRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToChild = {
                    navController.navigate(CHILD_GRAPH) {
                        popUpTo(TgmcRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth Graph ───────────────────────────────────────────
        navigation(
            route = AUTH_GRAPH,
            startDestination = TgmcRoutes.Auth.REGISTER
        ) {
            composable(TgmcRoutes.Auth.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { _ ->
                        navController.navigate(TgmcRoutes.Auth.ROLE_SELECTION) {
                            popUpTo(TgmcRoutes.Auth.LOGIN) { inclusive = true }
                        }
                    },
                    onForgotPassword = {
                        navController.navigate(TgmcRoutes.Auth.FORGOT_PASSWORD)
                    },
                    onChildPairClick = {
                        navController.navigate(CHILD_GRAPH) {
                            popUpTo(AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(TgmcRoutes.Auth.REGISTER)
                    }
                )
            }

            composable(TgmcRoutes.Auth.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = { _ ->
                        navController.navigate(TgmcRoutes.Auth.ROLE_SELECTION) {
                            popUpTo(TgmcRoutes.Auth.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(TgmcRoutes.Auth.LOGIN) {
                            popUpTo(TgmcRoutes.Auth.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(TgmcRoutes.Auth.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(TgmcRoutes.Auth.ROLE_SELECTION) {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        val target = if (role == UserRole.PARENT) PARENT_GRAPH else CHILD_GRAPH
                        navController.navigate(target) {
                            popUpTo(AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── Parent Graph ─────────────────────────────────────────
        navigation(
            route = PARENT_GRAPH,
            startDestination = TgmcRoutes.Parent.MAIN_LAYOUT
        ) {
            composable(TgmcRoutes.Parent.MAIN_LAYOUT) {
                val alertViewModel: com.tgm.tgmc.feature.shared.AlertViewModel = hiltViewModel()
                val parentViewModel: com.tgm.tgmc.feature.parent.dashboard.ParentDashboardViewModel = hiltViewModel()
                val uiState by parentViewModel.uiState.collectAsStateWithLifecycle()

                com.tgm.tgmc.feature.parent.presentation.ParentMainLayout(
                    onNavigateToGlobal = { route -> navController.navigate(route) },
                    onLogout = {
                        navController.navigate(AUTH_GRAPH) {
                            popUpTo(PARENT_GRAPH) { inclusive = true }
                        }
                    },
                    onSendAlert = {
                        // Trigger SOS alarm to the currently selected child
                        val deviceId = uiState.selectedDevice?.deviceId
                        if (deviceId != null) {
                            alertViewModel.triggerAlertOnChild(deviceId)
                        }
                    }
                )
            }
            composable(TgmcRoutes.Parent.PAIRING_START) {
                PairingStartScreen(
                    onGenerateQr = { navController.navigate(TgmcRoutes.Parent.PAIRING_QR) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TgmcRoutes.Parent.PAIRING_QR) {
                PairingQrScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.APP_BLOCK) {
                AppBlockScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.SCHEDULE) {
                ScheduleScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.LOCATION) {
                LocationScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.CAMERA) {
                RemoteCameraScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.MIRROR) {
                ScreenMirrorScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.AUDIO) {
                LiveAudioScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.ALERTS) {
                AlertsScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.WEB_FILTER) {
                com.tgm.tgmc.feature.parent.webfilter.WebFilterScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.ACTIVITY_REPORT) {
                com.tgm.tgmc.feature.parent.activity.ActivityReportScreen(onBack = { navController.popBackStack() })
            }
            composable(TgmcRoutes.Parent.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(AUTH_GRAPH) {
                            popUpTo(PARENT_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── Child Graph ──────────────────────────────────────────
        navigation(
            route = CHILD_GRAPH,
            startDestination = TgmcRoutes.Child.PAIR
        ) {
            composable(TgmcRoutes.Child.PAIR) {
                ChildPairScreen(
                    onPaired = { navController.navigate(TgmcRoutes.Child.CONSENT) {
                        popUpTo(TgmcRoutes.Child.PAIR) { inclusive = true }
                    }}
                )
            }
            composable(TgmcRoutes.Child.CONSENT) {
                ChildConsentScreen(
                    onAccepted = { navController.navigate(TgmcRoutes.Child.MAIN_LAYOUT) {
                        popUpTo(TgmcRoutes.Child.CONSENT) { inclusive = true }
                    }}
                )
            }
            composable(TgmcRoutes.Child.MAIN_LAYOUT) {
                val alertViewModel: com.tgm.tgmc.feature.shared.AlertViewModel = hiltViewModel()
                
                com.tgm.tgmc.feature.child.presentation.ChildMainLayout(
                    onSendAlert = {
                        // Trigger SOS alert to the parent
                        alertViewModel.triggerSosToParent()
                    }
                )
            }
        }
    }
}
