package fmgp.did.demo

import zio._
import zio.json._
import zio.json.ast.Json
import fmgp.crypto._
import fmgp.did._
import fmgp.did.AgentProvider._
import fmgp.did.comm._
import fmgp.did.comm.protocol.mediatorcoordination2._
import fmgp.did.comm.protocol.pickup3._
import fmgp.did.method.peer._

/** demoJVM/runMain fmgp.did.demo.ClientDemo */
@main def ClientDemo() = {

  // val mediatorUrl = "https://alice.did.fmgp.app"
  val mediatorUrl = "http://localhost:8080"

  val helloFromBob2Pat =
    """{"ciphertext":"p7IOfTNSU_1wtP5Pbjt_z99sRckrtBCX6fI7XN7o__dBi4jpl5IwVT4gYsj6Hvus1MhJi13eFneSU4YJKT2B0KXkk6JVGep4UmsMiAy36v9ZUeFEhVS2DieIa9j3_8WzSevFRpMvC1RKwzFJ4Hxvk5xOXzvLRUa6ty0DMzH8uB-X_35oOwLmgTp9pysioel8i7pfe9CIR9DSsWI0EOt0KesGfRcN__-ob9u9aXrsYvuOtq4ZLLoZLRW7XY8xA5mkZq3hUkoB-Uq6hdIw9-o3WVfDC8cVZ4p3wavaW_nfFfwe68jXz2bgoJ4t-0GF3O6z_iUkTTxljaZ4UgQDhp1_wl4a5mqhIiQzziWsY0uQB6sEIk3sSUi71tgiMU6yrUqfbMYI9s6V-7XsdhMRvLuqY8-IClKElsmbQBAxqap2R9bFqOylyqQxlXz-HIFVdzseZV8LKpiGORPg9us1xaIHp6chpFUwm7EaVXm_YzOgjFGOp_QPoGeTiZfycL9C_GQqddnMjm6BhjxQSN7aSX5xxO7ma03pl8oqQiSYpoMRr84QSe1e7FGna-IOydRGNMHXrLDLcRlsIUtxgLzJ1B3MK6iamI6jT5AUqQhF77qvgeZhbtIJ83Epk7bnF4dgngT54TYDaFSNJv5px-zi_nCYe-S9Pi9d3iWuimLGXdUfiwC1344Hqe4ReZFNTP3nX1NIqZHseMmCBLOpeJ7iqBeEZOo4gR4cdicl7z3h6dQfDnDHU6N6yOa6IH3hQDIlMKXfdfqY7OeFmg4k2LUSirUZB-6USw_vsrhRLMjCnDRmQEhO7sPv3qPN_7t4GywzVEHhvqLVEl9yWfndEdK0v7zP5HOOgrv5H_s0c_GAzzVo-Y3zW1FqGDHwNgUUymbx8xeJV4ezFKlYS7lNOjKZqvwjmCUPzFy7A91AWGaKiNv8Lh4Bc0e_ZWUIBTzyWvXrg16uBOyIukzIGnslsLIIUEuxahjyYUaD5d-byxghpzlNGwBi4xnbW0VgDk60gOkiuedvl-fbkRBKOZ_SIt-NkV0POr5dRPwe2FxhXCADcE3UUmyVPJwHV3bu1_jeXexEoisJ3NQTfbE8UaIYGuGNfnk5jMyh-3BNbVWNk2Cr1cLngfppYH551Iiwu4VNk_M5oQSSSl63UK7raXQXvhU8pBex1w8BDv3mMFDxW11Yup9iIpXOey6vtvn2GttLi3bS4UTWKxtG_jo5JRldjksuUzIeqMq3A2AqNiEjrVWEcTPUNuNN5M47ykusnuylV2683dEF2u6yQ2thudSCdWUQpqTrtkQ3-3YZZc6xIscD4uWeL8YQnvHSpaZGSH2AkiPP2WOhNznPxvq-ztxKc9cfp0Pe08QWQBY-YjH4tDgTq-TQtMcVr9y4NHg4cOBMOkerlXHnNzKBCEFSM_x5JJm0hJUi3npb8fkCjDmueYTwJDNhIl0dXGeoqHfU_JysZ61J64PVWQk74_MNirSQjOlzW5Q_2OsyWAhP8rPaoDU-6-1taGJBtLEDZGp3VS_OvEe24KCNF9jrk0IoavwPV-HWymd0EUKHZ5ytxS0IHsoQJ_YzGgqWunyOy4uu6Iu1kafviPvIpUcezBc0Pt9w9AdRXwqfZs7XsLj1MfqOTWTrhpazCI_lmJIsXtg-oP7rdqEBDBc7bJkCieonABBPjhpgIjEnbXZOMkdwDQmGFRGWKXj19XiymAKYu3SYQcIHC5XNMKzoDSLOmHbtfV0DUcCy9B0yRLVirwyz7fcn0h_x92TNOdzYVAzE4q4erYPZHMBWq3WIyWsZVHlrp0l0tR-uE8vdKTjX3yqf_18Ss6m0TxQr5_bJfweKg0qyJgEyBYjTG-df1pJD6pyJBD3ujzq0JgZKZcG8jSyLRIngr4uRJl4tFKAOeAI8_zWZF0oriM-YU7Ymo6NbNL8x4kRViRKx93GjToruK4d-UUhlvPxKZRFELABx7i5ayL2lVZHmXRVjGUnAVVQOSqIRDg_oxJbegNSw_cHoRbm1njF_omtNSZcYF85g3YLAFWa0riuSeBBqbKJrF_XOQ4Bgtsneoq2AZvl3j8lWnp4YnTdmUB27XhWdlF_L5FQP2_IzqYeFBonYVXHtkqosuE7Tw55z47ooLek1fYFwnFZ917MXQFiE4pdS-DQ15NhceLsBKGd_pDYRz33ClggI_8P5fmt3iTa7oClrA7DG_zwjkTYZ12m6DjmvSekIJoBwMzve6BAFl3CrKCrECLoBSk-m_CAONRo29u11E4mdxykIB72mc8R-DTUD9sWoLv09eA1jg-2aL0TTYhEUlEMVs_euUowRkP3RXNzwj1968wwJWVSIzO1m7JcNznWIvW3T0qWH4cCuMXcGt2zP0UppOWoITU-Pb3eHsbyZ0r-vkdsRIlS79nX80cHcDeBHfc3RoZjr-0k3sXgy3KMZYe-8YH3DP5XOaHJ3kiw365xToLljFAeWPAnn8wYE5R5H9czfIMaYNoHITTRV9I7xhvJMhDrd1vtGgmfR08q1dLXRudebuj43mNQ6ECev8SjkVbXyTOf3ey4hgpQVff3CgSmIERzAflErjmv5ziDIh_T1v6xsBPiBLkbLZ7BizFlE7r5xqb_R6IofSti5xmWmQTKRzV7k9BZLRljU7LoEhRbEpjos2j5fENXGKMnRcbAYwaSvBNMc3bJ2bxRY8oYENYz3Pf4aYDqwbSx57BtdYrwgxYQi2W6Yx3iw_Iy-cHsnH6l2Mpe11k5fOKRz304jrHgDbmD26iU05qtUcADXeeHFK6fA-8zY0NQdCM839HpaNlk8cnpBrAGDZhmZ551qTDRt5z0DlH9Phak5dZzbl2n8xGhgIvoS0ShG7iEquaSB-DCIt2lQD5uX1morn08BRwxKlVOz8uBEG2lfqkLcDGF8ipUzoR76tjch5uWxyI83aa_UxGJOm37DKD3p85B2yf1OjJuNpjmX8afVXo1E1-Rce3gZFGSAUaSHUwU256_qxLMqnkF2YO6NqTBLnwWZ-T53-rH5ZE-qXyA2qHKQBZ8_emkiOJu-EZwGHkRdf3JigEsyLTBTf3dAX9hqLjFRv4t6nT0yCOzyYu-Lj2tM35_24oai4MnYVyfTmmgynsv0wM8MAXQr8o7UV58Nyy-jbBtjbdw-VWSgRg61U2ntflb_ogz-OiW6myGPAFGtvRhefHXBte925HMiT6X_hDiIWrEdfvn8Pj1t2oldCWSZ2F0X04sZ6sLhAImz9Erogmx28a7O7uAiznWPSsFL24K1Qq2UCAnoQr835Au6jrcvnCIcaRN7c6d9tNRR-vK3h3PSeymzTyq04RU5Bxr22GdlTbepzPoN3FwOTWvKs5b5TumF5Mvb4OoALsFiQ7DtqnjJgsW24WXqZE7xgy0PhYoCX7gE72_MxBFmUJ374dBqxWz93ZaBjMwWhmhnS1LWY_2YKg76PGG1eqQ9n05UEuW-VXXIneOQB9rrKAzmoPEiaLQ8ISorJxfPpljFbjjBlx7if2BFISdIILCrw-3PauMHdHFwIw8M-nPKLdZ4lbPWNEEEaltdqsk-uqslUcCeZqWXRtf41HXRxbNMPhDLyPezs4gEP7C7JXe-sIzLMhAZluR0RXQOnn33mQhRA42u9_TFkSjU3NdvlPcCyN1qmyvsF6glt3PH96HFJUxiSEJWqzBU-TI8wpKszwo-sLNxZN7OW8X9Xyeh8LXCRogEYLSMzWJe8LTvTOhIjOVIHdttdOxRBO1u0N_O7j3L9AxWcqL-rLa6kSqAx_ng1GpzBkOHpbb3I-Iu_ENu4qnDjSJEtcJMX2TQv6LdpNzYifiy3wdK4siap07xoMA3J4BIM7UmftiAYegJ7w38ME9QE_prEb9HscLwN-wAVo5UHqpI432QCiZOoOpboF8d5vkmTiJ3Sv-jXtaBUnLwC0S9PdINhCj4WbcuTE1Bt50wBe6HQ_rrq8e81F1piaNc_WkIhJBdxcalRaRzoIB1KFPER-gQQD6-rvFkzf70icLKLAkO0mqVoUmwezGJY1HWHFs-Ixi4r_nR-AuO0uzXp9bSp24bCkjrA-LSyprkekNlkRDOu8vMt5uotAScgk6Tu1_-ZTz5wcJbNfpkcrR51mYI6m_DMRmhgmNlr9yQOnqNhf9b9Qa9YhOe1TBj4B5SvYSB8sMWKEXraEL8o5O3uVeYC1EuDo6nFUTitIubDkgh13IrCGOj-D6FUlWUuyOiBpa7fDjrwEseSz29CFSoKXl07_rFVQfcbdV_gYbtgSux59N-37Vmz7rH_vsTvWm08Z65_h1fHBouHPEWcKLzSrCXwE4jKBc1CylsjSlaoqF15gG5Sx2BGjAO2kLWPA-AqsA1pyY26BguE9zOYaGkMA7U-QoCHgNK4XEDpLZm7kRqz5nn2twB7Iqx1ftaDAs3JWWwKo2wsBEtJ0Z7hzueLGU8zXfOdBb0INHU5E2cyAIcXECSNCMJfNIvr27bVJWjTALXBRlL2EQF-3HzSr97l7u3Iu1pkiCbkx9bdnhHgtC_EkImO1xePu7pdRQNmvMc98XvJEP56pLcnsCOCmrbvcMurfi1rcTouVB74Q3h2X-uGc22l_KI6adoVq44rRFoi10-TOeYc7G7CpuiOA8skuZJWVykQR46d1xSAloDCHyIPhG23rfzQYCH_Rkapz6VLcvxEKCmIZOiybYDZ3fCe1RZfV73LmkxKgkboou3i3EcWTdVPQQNa7CardRZfxlOs33yKPTpmJVh-5_up64gmMuPNX9xZEY1KWs5ehiJ8lkdosrlxpEnxXTt6S3g2COSR5r5_fdR3lGd2-R5KL-FlprvGE1LP_HajOEU9lvfJqGAhMa0ddkAb9AT2hum_bKOI6Mk4_wT_hbRTImwg9B4mxOaQE5ng2fTBLR2kH3Vn-JkSYlOnk5c1SMVPbdcJeJlL0yoMUG7QvvxL2PnN4UKCi4LO5DWcIkCtNHB9n3wqq7r34r1HfOJLGAc2IV8cpWCbsA2viv4x0JFO6ulv8Tb4N_YJEDN6ua60iEyp-iEIQc0TAMw-vjqExqgBp94AZ7LcbYLmL7QcJqB2tW-N80yugZfDvAz6fg2ptS4lv6-yWUNBS7Ggs7oX0Ao6F1Tk-EkqSV1sc_7jGYMsbcOviDnR2_56Ip5Ig","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6IjFLZDhzMU1CVHdHTlpFY3dFa3g5bVpQSk9NZTZwanBhb3BTb3EyLVR3MjgifSwiYXB2IjoiLWNOQ3l0eFVrSHpSRE5SckV2Vm05S0VmZzhZcUtQVnVVcVg1a0VLbU9yMCIsInR5cCI6ImFwcGxpY2F0aW9uL2RpZGNvbW0tZW5jcnlwdGVkK2pzb24iLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiRUNESC1FUytBMjU2S1cifQ","recipients":[{"encrypted_key":"kPF48-d5aTRUqykKjwp4IwkvwS2Sq9XxqXRBqww1jJlVrEyg6sVRpyDXPm55O8MIaAygSSco0KejFdWp7ItQRL0wWycbIumC","header":{"kid":"did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9hbGljZS5kaWQuZm1ncC5hcHAvIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfQ#6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y"}}],"tag":"oLaZ5VG7FYO1ueI2J2cynje_8URVu7josZIwEgPb7Dw","iv":"oD48V4CNL4pEEmQ9wmkfcQ"}"""
      .fromJson[EncryptedMessage]
      .getOrElse(???)

  val helloFromBob2AliceExample =
    """{"ciphertext":"NJv42CiLa5WU81o2pM7YvyPod6dYs80T9TeWnDhww6_wYFrT6fs5aQHcm9pKZeqZxGLIuEpoGJZs80hQ-_EDL5MyNGE8wVskwggqdATAC2JMuUX_KSyrojrxPkp-tCjH8lRqVZVUhoEipnNPw0CnUSn534FE77R-Ocf62JwCSk-9dvUmF9GpMU8K6Zzk_Qvqehy_H9_FYdpVdcxkErGGa_yKVnamAD57QOTKYjNC9aE4bP1iD6KXIuEirkVOwgMRNPNU0xolYJTI4HTSaQ9UyYsqZ_j25OOCSdXCZR9wfajZUhJFsAxqLvhxP0fxnk_8TfUsfLH1KHGH4OUkXnABksyXi_yOTAlk_Ld2__Boo0fLKdpzGkOUJxd6pGy5vMNpE_SZkoJ2rQnoUBLTbjUU6uTWNDgD7Ns8hY6Dm_3Mh6_4TlOxUxyMxB7w3FKEWdB2b9SqUcDy_eMM-DesSo9lf9VrlsBqHHBVXZV14nhzN5Ii4EGrD5yMdto-unaPXHK_2mDkTtWy-ylPAO4Ej9FH7bT4artyLigWgKWHiPLkmfp29vpjvxD_RyXCGw7f5e8EXFvT_xF5A31N9OaIypgV7tsf8e5HqizlL3YSlB7GeTgTQqESqyhxwsmqtlPINWnifJFny7emRQcczecO2Zja3SENpY0IF3jOc9LiKY14AvZmbz6-Jq8aS0bhB4KeMIF8XJlxKnfhlJjA3wQR-VNeyrx5ttOOUEquZO5fjEqGwJzl5QW2oVyNSYHDNRVgu9LYh5_DARbO2Pf0e_1SPGIJMD2_6o9MYpa-j3AkZqasIvngV1JYTwAUQEJBRqo6-Ss0vWfVqDDSg44cCX0hAtExQp2xXq9Ctful8OKMGz0rZCqAGPaelkpyvOyz5d1qCWFMXwUCtvPaWv0z7DUt6Ual79U7wNfN1x6X7Yyo4GdI8zBwDIAByRcPDrx05Fd7ZQeeA4baBhgBgD6T4OIUFlg3gv1-IB141IfS1fO8WMskArXrV-jefKF-UEr6ffw_gXdiMP4jehhGSEO34N9MqFoAcdzYuNdYIfIej3kTZBoWkKOj4r-taNmJWLuM_QWAQ_BkgeX9mh_GqSvdLF71rvEprTdzZuokqgGc3LV0AhJ0bHdBxc3nkvSVsgFJ3vqfJ-3eQsMyDiXmNXaAtGvn1lkJ12TwvYEvYdJM4f4uXk5Sw8AmUfYobX7DcBUE92Gl1_xmSW8720XDGZzguvDP904jntndpJt4q81OymuDxlkBD8YkNTm-pj8emanv-LuESY58-gKvmgydl1HOnaQFzz4tB166Nbk72uowtU5pos_HYy5HKbRcyvwq0aRxVZ_a8-GMURhLxpVvjpaYByAGFew_05cthTJ-ix_kO4pUGK0ITDStJiF2ODF9wD_NhfmQAPZ9y5f9h-PqNv7-u2cMqaVsmOK4XgEOOhykzpWio1egem4fYVVEB8qUQq_syWMMwrCSrJRbhoYVfa_a3PjY84hOVH7d7qogaom8tG3HbKRr_xKD1SEZlAs24Q_XOsGvjCX8mqQO3ONuL-GmuX71QUET4pbls4JTzyvBZfxAqtDmbmiXNCa4Ay2ebDUIW-ecwFylX7wYUVArSWEUSY8klPFkJKZ4bBw8ZLeP3U_u9Li2S0RYBkEApker9-QOu6U8zgBF2xokNdfFLQDeH_JL3sOyOBCezH0QZf6w-mhdUfJTKB_XPP7fqSHV-fkCSZtUgEM2oSq-FrCcB1cZebxR07dY7oEkJgGDOHCrD4DVDQGZAFyPAjm4E4WkI-Yz9ZyB8s_CDNTcOguXcxsQb-rfsutQJuoGCp4oNOJUUW_PI_nvdQu_3BDz9Yw-Cke4NunTuVHMtPnflO8NlAOs1utbJrbSlxoZ15rE02I5NUXDY-49PXgqnkwMXpnfO3jYzix5ddnjGR_ns8GT4U0JsN4hIULlWY1c978perWsb9b-CfgtHy5fNJBa5DDyc9QzEZsD6ECFu_t4TvbWYXXjXgTxZP5U0jclP59Tdq0LcI7hOZ-e5kTS0wwTJMPUnjBQxT5s-eq_mNO1fsE9zGqL609dEKKJNFDRwRuTkTQVIH0KNgdDorIVN90z1MlRzJIbpDH2howN-eH0h_BRm29RWOS_yoyIeICilyAyRBEMO0Zf3cSXYE39L2X4kUlWQ5HmEPcv6tKSR0xg_OuKCRaCloJY6bfQeLINann-cvXN4_yIDeVAIAYYlEYQa8-vry3sGqKrqUOHrcSy6FCxDoP3TNX_DV4PJdtIgJHya-miadNQ6zynQ5vzJiO-FUOayFR-31j5rRpKUkFSFf8pg_jVEu6H30X0BYgLv2ib1Ac0c8PHmjyJ6-nWcEUN0fw5XgTo0KFf43hmQkF-G2nhXlAA5TFs9Fp4Ao1Zr4FCeklrzxvWCYjsqsiksKoK2yMEWCs5B8matBv5lK3IX0nhjmvc6JZTvL7MdjdBN-BlVmAku-5WqHhASbRePwu90-F53-SQw4OlGQHMT_avizO_t2cx87A8AgimwOUhFxRHkedybaRBVGS5p1f9LH4KSwFKJAheYmA4nCno1vadn_04rvf0G4ifQmuP7jpgjtL21UbrBiB7xNnwTerngIVNwVeVSAHwZaXCRs5tp2QZnXDI1tpSSFG-QTw092oXcyGCq4G8Cm3iBpP2HIPpiwPmYMy3Y_7es8IHILSWYfUunrgRdVj84fQqL45sV3c4_1WDMA9vB15p5pUIwiuhYLo47Xo0oqY_muBXfIJdWVie90S60dXU3JpGISE-AhyUUaA2Lz9GMVVyOfb5gy8rTGn0j8CKSxS4N-ZmUPwEprq3vdle14nke_oFEki8qrFUrEKTyeCDYNvUrco_Wd6LdVc3Hq1jMRV0euzb3fUFJF86yWUVtfIRfUJlE7G_bIwf0UVGd42aFssDszk5lnXnpgtnNdiS-cL629tbgfvS0QPRVOG486E8mkgKMGJMVHEBGV3yUd6ASjkvQ8UBNDq6j0zAPqCJHVM4W6hBhWCnBv_ai_O0gR4Zc3Ah_DH0Vl_YVAXAYQBzUPlfyScCKo7ne4M4fLT44Kq1eTjHcfgfSXEToKMnAz7h9Mib4NGesrlXEyE7WbvTWOm78pwSghoa2NcUodH2dAjXmMgdgTeSR0qcq0Fcgb7YJ53nh1rArrKdaTUKH9AJFnV8J2yKQoZri8BXh1Q1Aiq2TjVcg1rRVxhF5AKeZXWSsHkvLSPd3GXulT4IXPFyj6RFOXxXU28zJDfKwOfHgKeZmf2plW3YUw4v7kHrIB3xPvwjXyVJCNV1rtAaI1A3k9W4bH9sTNcSpTSB4jjhC-5XaWFX76O-D8IUHVj1qIo0yFjcu80iom2YwE5WrCxgVe3m-3ACWTWHDhP4aih_lurlj8AePEhOcoqyumo_-i0gm2ALdN0sPFV1L9FQqK0yIp8ZNURUTsUmKDkXf96NOfuOCEdHPB6C","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6Im01V3U0SzE5OHVHOGRLbTN3MFFZSnRNMXRQZWdJRGRtVHZRNnVOWEVKeE0ifSwiYXB2IjoiLWNOQ3l0eFVrSHpSRE5SckV2Vm05S0VmZzhZcUtQVnVVcVg1a0VLbU9yMCIsInR5cCI6ImFwcGxpY2F0aW9uL2RpZGNvbW0tZW5jcnlwdGVkK2pzb24iLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiRUNESC1FUytBMjU2S1cifQ","recipients":[{"encrypted_key":"GqQ1K2II32v1GzJ3H9EhtCAbeg-tkFgy0VZmNkOo4FP2IKLdYNmZPAM45VcF6P4oey03CTIFg5rO1aCKJ8UbwrfoxL_FXf2z","header":{"kid":"did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9hbGljZS5kaWQuZm1ncC5hcHAvIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfQ#6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y"}}],"tag":"Nz8L5vax008BioQtqst-QYjIYlltPSugsI9PA2BW09A","iv":"wc5CnQZusyQUtNJew-G8KQ"}"""
      .fromJson[EncryptedMessage]
      .getOrElse(???)

  val helloFromBob2AliceExample2 =
    """{"ciphertext":"pkgEGSPTWchFA-GUQyCTtz5UkqS8o12kJJa_1XbGVqa6xVIqjY0xjuMRjMRlwReiY2GNqLWPeT6GT81-fI4u0tXaD8J_PkXFUFgn2zU8h76CbjzSbuShdSrt-1eAbJTLjhPhwYlxdTmLSt-1kBOMXGup7gnaf93vWARzDGBdZKyFzTadLTT930XbXM4H2YadmwSlSSAozPBQsksXxCQrgU0XKWJnS2GxtxXPiuat6lb5kn1yfCfGZ-zmJj-tlqm80pX9w2eGdSKD6Tw4Mu4PHe41XDcomhKfJyyRY_bevl2T6y6t8x-m8JqiI4O2aR29sQx4SJ6Amd1T4xCI1GewPChT6z--LyN6OziekCKnEPbLNr-XHIZaULw4sQFSRyrEaAwwZVjTt8uGIsUtJc1QNskk247JK7Uo7W8-j9GEj7lsVF__1oo5XHgHeWcyn9xly-cIMP6dDXqs-FoKluO7WGGQECVpmnwKMcNA1RENzkvekcS7dIqN2b8dRalysVe0fQAPrKZCBIcJ4Zekg5hkSJa3vjwnIZBwLnNIgaNb1rmp9koJG86hgnc_useEAh_b55T11lCwhXnZccST__t121Ub7JG1UjwQ-lZ7JutcE46lOeJd6QYeFoBeytdsrtpYEbwLKGCoOLp_IljzK_qrZzKtiIyiuA2ib3GvgYTHFTBcWNuJfm-hec1wesrkCc3tyk-AeFx0ikArUss1NB0XaLF7jG0Iy6cYjwkUw4aE9t0OrkaKbbWumerqW14nlYbDPTve1v0g0GTGzXAQvVXUUKCtb26GOvhzoAb8cJ3MFgclCclpGyFy1ULjakoWmm1FZamkeAG_f-qZMIzsMrJjFze0Pf6C2Az0uAgTgn9tyjIrYWzHN6VNyiZmhQ6mdLywjKjp-ZNeRDSD9MPPUV4bkTljWW7jwoe_6OQzY-czpXMWDgzB8ELYHndMoTZ-BpJ6bxEGNOwXojcIte5QDtW6AMjB0nPgKSwV3rhoVxvxnqp8wKFfzsP_r1m3AqFvfzEa-r2ZM_PIqHgarXa-aueh_lAXRfwS9KphQwpPoywFTS2xs4IEsCcprJ18g7FlUrBzLbtZysgfWxc0UJbMf06N9DaNKRayVuAxhHxltSTLQ6mTSCsQMKRyeS9kKTJSnnbdqyvfKbiVvSW40bvrScTMHJ-aakrvHo4d-sN-fVeH4_9dnI4mtAnEtcdUlm-JUeiinEfjNRtZuw_iR1QcKQEX7mjnJ8zFZoMvXDXvXE9UIwwrdGrs8Kfq7ttPh-C2IjmRpIlIwjdxQqzV9vLMKVg9Hw-a0VULRIPsC875uGIqgGz9QHwCy8Pym5N6IOw-9MUnaw7FKTghFwR4OoNhLdnXRkMJaPPAwdu2wFGQYUxlLsc6pd0OXMYrUR9tksiSJrE5cUHXmgEofRGVL4iEN7nt1cIuEGZLRmFCv8-wID1gsv4NVTIin6ynU1wfe4A-1iGCf06ny1LQDo18fxjDfaU1eJoYvd8su6u7ilMirMJfinXexTI542-WaZBz4LQFe6rou8UJntDUY_SmWpNVkU7x9NDMmIPgvVqeON7HUUbbkSUgyncetg1oMCgsBuZ1JuVfGEzh89ea_ZkEokYZQObkCOjGvgMrc2NiImjWoEKAoeqVYm5bx4kAabfhANn8Sa6iQ8gzNX8cqoifErlRCipzC4SO-axTmrYL0MfZVwm5ccgH--4RAsTnCM_hcLdZXQSns_-dLqtMxyw8qU_JRok2I0wvTjyB8ErGW_u1Vnx8ys0E5NRZmaNjp1kLvWwxHPcgnr33h-a7PurhmE_XShi1dXeMZ-ZfyXfzoApWYF5WlW5C2aWncieJzsf3ccX6LvJSxaR_x_r-lXMq6GdUN2XnBvLBfL9G47yp3mxcwJuKDRxhH9tdyjikLNE_8pGc8VcxGsZ8aigC7NGybzf9MUSDYJsvn_ktqnHRNN47-Cv75nbucUQXibK0E3pX-V4OQV5k7M9ExqTY1DW8FK8YWCgmxLG5DbNBSEM5zsNlfoGm0xzpSj9lsm4zkMQGDCh1UXBavVJXSDwvf1FEe5zFP8m9h-odbrBoyzofgdxMABmBgoe9RxjT4KF_i77ULGQ1M2lStZJsNvW3WokBT7Nt9Ys34_ePEzV3FKquVGpXaRrfW_IvA2qFNGZVLz0DOxSq-_r5Y18M9EniwnP_RGeK9hZpKKBlACutBBqCTIzhofggsbz9zH_tCHmiGM1_Pv2wk-ju_ct-uamMYQAhcqPIR7csnveAUexpOiNOQ45MxsUQl2YbKZQunrv8oZPHz0HBxNocwlJ7L0AQovozLSqURlamyrYdwevQch65hBoR8R6FELPlbRBTZd1vKdDD4TOxx3Xd4HvfzegVmv4Mj-1OVa5-uotSWQBOYhye-2-Fy_34fW-p1-5hx4HDZbpcnULJNNAPJapJN22rKD6S7yn2VjyB9RjhQV-ThYU0GNB0QcvFbwH_DkCbnxVsbHDv9JFHrrQmPRs8gSScLioKPVAW2l5WaaQo8Oks8eyytd3wMTIbZ5B06CuhQe7pmIYM8o5ElZdpuOPSe8QJjkY22tagyHn-2WIylZbi1-4xm7-AoK3_26dvyX8SbJ6DMWXwJyeus9krG4AIFsorOA9iXPLAg85wQpOJSL9yYiEFu50SPEU0nj9lD1mbWagI0nTlUwkKPPNp7gKgDWLJCnVnPG0r4oaa2W2S7g1K1hltflT7z6NKxVZwQ_xZmur_BSfoHA9gAb_KFa_-XSn1jYhy6C-VxTnTyyrR0BoIRRr40s80XLgdEgSwZqBhcyHSINqjMA-rT9o5U2lxRD438l1ybYLUmMyhrfMh7YGvlvkj0A70U0WvgbzL1zwSk-5vKglVfuyix2MtJ1-S_iwvxkgJBYnn8Bg73PktljJ9328iblS5TWGNAisUPDuHviyT5mdBia1tC8v6bD3vvQGUTiHxZ159DOrz3ZGFQRWdk04bj1GW9kv7higHcWcUt-dgV60jmf0Ed_S8l3ul6sXK10YGBTNbrD_pO6JFgI7ODuZ7Dxp8LybkL4nnbcUxbsbcuSh1gwQSsjvpHdK7KuvJgZroF3hnd7-uCYvQ3g68O3YidskOo_Wc2Th_Hpowo5hbgeZrkc1mtLmliu7N49CGLM7GVsY0XQ3iZ85xN_H4CETAPhEUsao3PQmdvWuxPxpVw38MB_qI5p967psnc9I3a_RuXpqL4s9J8Jy7cdJOl_k6jGCl4z1LGpuRpXlywtNEdApPIUNtWXgzJkV4oCZkQOXnz6VRuxr8DY5UVgLDs7D2W_4MBcd-68-0DqJU7Lkt8J0sNnOVIK5rc50-C8xJL0wYBeNpGy0w-paY3PWIDWccRPGhYmea5PIrT6vvySsCbGUTwiymxPySKLg-dOs0LNjgiRJgB0qQe61TefVyR5j2IjxjxgmKGLHcFkDBJhxjynYJepAPXHg63tMOktZnAAYqt5jSHN8pDtUwyoeos8zTBDN7sOVuhsI","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6IjA2c1lJSzJCNW1GQTRKWkZaQmtOZjlwUzRaVy1MWU5HcWZpQ1ZHVUpFM0EifSwiYXB2IjoiLWNOQ3l0eFVrSHpSRE5SckV2Vm05S0VmZzhZcUtQVnVVcVg1a0VLbU9yMCIsInR5cCI6ImFwcGxpY2F0aW9uL2RpZGNvbW0tZW5jcnlwdGVkK2pzb24iLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiRUNESC1FUytBMjU2S1cifQ","recipients":[{"encrypted_key":"dEdpz50GTw1QKy2HjV0xNHc3-voqR_Ae7mOPuNHd2js1nQ1WvjBoVfxvoiVc83nqyCvHRl5UjTnVfaZ330AOOgTezvDFQ_g_","header":{"kid":"did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9hbGljZS5kaWQuZm1ncC5hcHAvIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfQ#6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y"}}],"tag":"B8Qk3QtQFcylk1PjoqDbwsh7PH5Qx6xXAvfZjzn36FU","iv":"6JRWgs4eVxLkKvMMlsB2Dg"}"""
      .fromJson[EncryptedMessage]
      .getOrElse(???)

  val program = for {
    client <- ZIO.service[MessageDispatcher]

    pMediateRequest: PlaintextMessage = MediateRequest(from = pat.id, to = alice.id).toPlaintextMessage
    eMediateRequest <- Operations.authEncrypt(pMediateRequest)
    eMediateGrantStr <- client.send(eMediateRequest, mediatorUrl, Some("alice.did.fmgp.app"))
    eMediateGrant = eMediateGrantStr.fromJson[EncryptedMessage].getOrElse(???)
    pMediateGrant <- Operations.authDecrypt(eMediateGrant).map(_.asInstanceOf[PlaintextMessage])
    mediateGrant = pMediateGrant.toMediateGrant.getOrElse(???)
    _ <- Console.printLine(s"MediateGrant")

    _ <- Console.printLine(s"#" * 100)
    pKeylistUpdate: PlaintextMessage = KeylistUpdate(
      from = pat.id,
      to = alice.id,
      updates = Seq((exampleAlice.id, KeylistAction.add))
    ).toPlaintextMessage
    eKeylistUpdate <- Operations.authEncrypt(pKeylistUpdate)
    eMediateGrantStr2 <- client.send(eKeylistUpdate, mediatorUrl, Some("alice.did.fmgp.app"))
    eKeylistResponse = eMediateGrantStr2.fromJson[EncryptedMessage].getOrElse(???)
    pKeylistResponse <- Operations
      .authDecrypt(eKeylistResponse)
      .map(_.asInstanceOf[PlaintextMessage])
    keylistResponse = pKeylistResponse.toKeylistResponse.getOrElse(???)
    _ <- Console.printLine(s"KeylistResponse: ${keylistResponse.updated}")

    _ <- Console.printLine(s"#" * 100)
    hello <- client.send(helloFromBob2Pat, mediatorUrl, Some("alice.did.fmgp.app"))
    _ <- Console.printLine(s"hello 1: '${hello}'")

    _ <- Console.printLine(s"#" * 100)
    hello <- client.send(helloFromBob2AliceExample, mediatorUrl, Some("alice.did.fmgp.app"))
    _ <- Console.printLine(s"hello 2: '${hello}'")

    _ <- Console.printLine(s"#" * 100)
    hello <- client.send(helloFromBob2AliceExample2, mediatorUrl, Some("alice.did.fmgp.app"))
    _ <- Console.printLine(s"hello 3: '${hello}'")

    _ <- Console.printLine(s"#" * 100)
    deliveryRequest = DeliveryRequest(from = pat.id, to = alice.id, limit = 10, recipient_did = None).toPlaintextMessage
    eDeliveryRequest <- Operations.authEncrypt(deliveryRequest)
    eMessageDeliveryStr <- client.send(eDeliveryRequest, mediatorUrl, Some("alice.did.fmgp.app"))
    eMessageDelivery = eMessageDeliveryStr.fromJson[EncryptedMessage].getOrElse(???)
    pMessageDelivery <- Operations.authDecrypt(eMessageDelivery).map(_.asInstanceOf[PlaintextMessage])
    messageDelivery = pMessageDelivery.toMessageDelivery.getOrElse(???)
    _ <- Console.printLine(s"MessageDelivery: ${messageDelivery.attachments}")
    _ <- Console.printLine(pMessageDelivery.toJsonPretty)
    _ <- Console.printLine(s"#" * 100)

  } yield ()

  Unsafe.unsafe { implicit unsafe => // Run side efect
    Runtime.default.unsafe
      .run(
        program.provide(
          Operations.layerDefault ++
            ZLayer.succeed(pat) ++
            DidPeerResolver.layer ++
            (zio.http.Client.default >>> MessageDispatcherJVM.layer)
        )
      )
      .getOrThrowFiberFailure()
  }
}
