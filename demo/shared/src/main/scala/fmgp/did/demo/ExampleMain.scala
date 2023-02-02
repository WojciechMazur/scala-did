package fmgp.did.demo

import zio._
import zio.json._
import zio.json.ast.Json
import fmgp.did._
import fmgp.did.comm._
import fmgp.crypto._
import fmgp.did.resolver.peer._
import fmgp.multibase._
import fmgp.multibase.Base.Base58BTC
import fmgp.util.Base64

/** didExampleJVM/runMain fmgp.did.example.ExampleMain */
@main def ExampleMain() = {
  import Operations._
  val data =
    // Auth """{"ciphertext":"U0giD6QwY6Pyh8sMaCjPHwho2QfiK2gGJHnXGqoP5zQzuEt62Nt8avycr6O3MUxNEjfjmLgVPRobPE5snLP075vdm6QmZHTQwqcoJ2j0HHJ9ptdHNTlS5g_QL1bGLU_lzAert2rmWZ7KWyGJ5DmMvpSJQxdiV_iincW2Jf3Aom6ghZIKORsG96a07AtR5QTRhJJFivvZirQSSItyOGfWBy0HmModna9SOJ34aOxN7ofC6ppIzjcSzHQv9dK-5BN2d2ydNn_4nBcdbvAeLylrYE126H7fdUaP4_eSQliYr2mEo0gtD-iphNDw0K3laVxN9GeFUwC80bvXD08nSW99eNn9ZenF7t5lXTPpt-LCNhhzMdHRc91O_4wfGpSQWxSzI4NtuCJdGqYxFM-6xQl2hnui8ILZx5LZsbelLNGFDS_HuufAw15wcGglNs73sXln9mZNIjVCk1u1wPScA7xSMTgxLdCuX49sRiNHR2TFihk7xm-7oqEHKR0jfq0BP-GEi5ACqXz2KJb58AuSvYnnpnPF_EkeV3PVt0q_FOdnFZfLH4GUfQEqzY6JmYko-csFgJxXcL3S3jwZZoEU8pZlIAmQim7xxffmWKbTcvR37oX6luxlYnM61sPjwnD36e2wFP2xS1c3WtYWg1zkrrEWFQ3VXy1cKqHTb8jIDGyPc_IxgWtOqvu70NPU0WnmWXBAbSlismofKKOPf4AgRly7CNdMb0z6jgRvtG6bCUGy5RzM0tcsB1-4tLaGJr-Vsi5YsvBo6zIZG9T_0iyEg-zZHKMftlCKIbVXGaHUqdraxpXNVf_Ooysz-TvpDsIk3aGDdaGF4vFrodQWk6jaFenGvsxkt3pYqm_taxRAicAmD5MQ1vhjWtOEsNBjUeakdh_KUyoU8iDxjBEi-aynJPu_hMFUNYnJq2q2st4_DZBA8N3WZpzuK6QYc3do4kOj3xTuwvPyAp61r1ISnBFtaL6zYyBk06gMI2umJHx4mLqYdSkFlAAWGE2wfH9biuJIZgnROpnqPStV4HLz96ozkD0hKDxcjwYGpsZZslaUGeYGMTMuBEPL0Tqe9_b-zX4sfLI0Xzz6dmu6XbJV7N4_5a2XLkKfW3CcpJfvjAPrV6nzx7goecY4TWkpT8YP6-EbVfxiBKRI1X2e6bKzhGGBTS8sf2dMpvIxHcQrlkICy23oShAeipUUCinSjAWN7oejgRTSSARJ_JCBOWoiKzR3ted-3l8heTvKDU1gtJ2XARe7p_hBxob82GnEqv_Xeo2_78huMxfvQ4RoOgqvEG7Ix8vRNET6ranYQe8wP03fX2kUlff2IUV7AFUrbBEq_uytouCFMFOLONi_vgBc8R1pS3Bk1ITsZ9iNX5kT6KZDq-lyE0R_WIqF_sVKHZa8Dh1Egrc2jQ4P8dTPgEtrSr2PNO-00LlT1e1DEXhpFOJptwwpyxgm62fAQevSyU7duZEpN9fZMOVGthoZdUsuyv-5FhxA1ZIlxDipLYqyhcw0Rz6xt_qLAEvt_N-t_CXVTHQxP5n5SD6pwflwUlqeeyeQyXJVm1K9DQ_8c5MbvMMKWTQ65Op5dZtXVu9qNrfrlXnpoRAgh3uAwYg5UGfs8DHLg5pNONYapkJT5RCpJi3jmhx2FoB2Sz88FBUmcKbuoOfYPHfnZcMH8PvHxGjVmADYAf4uKra6chrDiLKc3FfQWNQPl12IOb7wPY0rj07ywVqvp8-kdg-zkYgCvj29m28fTyJ-jbGBDMwDjs5aS_sj3b-1N5-zpJQpK4TvBXVIAjp7clPXCeqE72OOHPEbowGs2TAm0NCFgMe-DN5KEDoyltJDlq1oay0B5WzXEVgmZkCbcX0bS46Iup9Jay80vW5TpOvBuHlRD47GynQohd3I2Z9KiS5e-DL5TChdNqAzH6yDhCEuB-VCrXviXGssDCk9FjPFyEpKC4gY-CNuNk9LUlBxMpeEaMEE1RJHD_2xLj7Mopo4waaKg3w_Q-PGcjBexIV5qINmjsPOXo-MXwg9gErFvs-9QznEMQo5rLPoX7nbFKAKSOTpsArPmDskykq70OjXeW8O_j18lhqO1WX292w8OQU4hx3gZ9vJxthjXLODD3d7RwdrjGXet2SkMJztURsKFHfOwt1Aka_OHqPz3-1Ba8ipoI0XhTrLsRJ7yZQwxmjWyXHlAqJLAdDeMX0gRtR7iGVlfgNUDtiWaIXQyaaoHhXgHUOKcLQPDESB1ZL8SE9KEIgDe0a77HPk0ioACgLcjo4ft-o0d8S0mcn-zT722Z4-FA8Swc9Vp7Wfl4ksCxXA_n9WeQUNAA-_vrtQmPrTzE65AAGh4wKrEonjYu8rfjNT29aPmotSuMCFn-jhASfeuh9FVGg0nhRvbzOeIdgE9ZkT9yOu1Oiqlz7i7A6oco5RshocB2F_mmMzPJqWNTaS47L3weqoqewjtKpqbo1Fo9PAJNexz8fQ7ocvzMsAbJTc6wZUaNVI0Q3NWZP4yXQBNXxXNc7jS4KwGwmLiuKysSewuV1ID4O9gjiaKPhaEvKkiIvyek89cfm_9Xzf_dz4fjx1Jekc0m7ls_4rn4gIjH5uKWlc_f6-TPhfJGodA3wmAnlXn4mGYKnHs2qQ_f06KUCArPHLdAgQpmxaRps2hwCj3JusaVonsYQMJF3fr5OFqtmzflSSex3vamG94Calc6a3GfossXQooNiSLPqhaA8oYVUugnnj3s-6yJbkX_q4MvmQ2l09kCJJOD_6uJU2__2iHrWR0mLtHM6YoIawk9aq0wUQIV8U1N5srFPHlZoNs0dvVdDAVOWxLtNDPxK4eFbdwUYah9_wj5t06j2v-1CHvBQnldlD1JRW9b2fBaycv6nQlXfPX_PwLKuJZ7fYfBwEtEwMTw7VUHFsqYD7P4rpEDj-CAmqerQ5ck3aOWgZtEFU6bFS1Yw5n_4dNhdhReShrp58WTYObnAZueG87iJiolS59B4tsVoaf9Heu3WJ9bE0ZDWb_LP8Y4XRk2tM5aS8CLHojHaqSExE9633Z_11UyuYTy1QJtgkNZMn3Rm5BpsGxAhdsZWnE_1GCd59stp9k8Xo1hz8tMc1qt2A8Om0ZF1NgqNWdYFKg1Z7N_B_o2pwFEY2AYJaP7gY5t-dklcBNegf_N2lASxMVCoumG3o295okSz3XdNaAb32Q-a2G52-L_Vfi-tYqiIk-J9Y7I95Yxeln9wgkw7BfKd7e2-B65X52Bntp2ht1qPYwhcU5jDmaIdr5QSDnymU5AicallzwGsD1J7y7c2vxMaI58HeGoVwJXthZl2lRuM7Gud0nLF-n9AsSz5Hi6iEAJWHjpxE6JAuwWzXQO0_WzTsnDiCrcHCNlY1BMaeTwwe72i5fMOxnbedh-q43SPSdS6xwa4jMdBU_UPxeK86M8rsEJCZtdmoEGY9Kv2sxXZnzoX_AIfLzzcVQfg_oxCKwWjwMDhMktDs5YRKERGuqskuZn3AUoddMJd11RT56jcDkqR3fB0RnDXl60NV15f-5yo0r8LG8UWzVDQDq0xbbyBIduql6tK6xJUKFO4m0l4hTcNIYHQtUKY78SeIYq1oBy2CvMm76LPq88Al-7Ssp-aJcYW1EPrPdDU4pV8oOLZH9hDL7cTA-SizmKxtZgdyRlvwALICrF2tKRFsT9KBX5hEz4CvQYYLCQfPjg2dDPZx2xRBuAdXkiuwx2Rpn9fhp4vAnWyRewpLd90Cm7CbYyQOJCK37AtJ-ZFNld_5mL5yjuITI1cgSbyCVcagrv75eu8_XVSx7GYSb0P5gK4yLwBHmQgvWuO0AU_4bntsSg_F5lCFcJx7OoXfplbqfPjzlNXjvPAf6tx0xl8zTOD7-mAzbHXwyDbqDejSnR5R_8kNDwLpb-_Hi8pPQFlEjz_vVSwcsWdjKnyB6GkXvBk2eYW1tjeX5Qkzo7qbLlDJfGovSnm6spJfTjAVnbeq1bjKOOr4wPcft7V7vOyff7_iI_oQcf0Cv-QOSaptWQS9EBXQKqjj_q8zIvnpDfOt0b8ms_vnPs16FkKXzXh_0lBWxKyoOcAtfbLkVUILpNYto8aawaD_N421sbL6VXSx1XWOiVxO3xHiHpXc6MoAxAzZB3fm-mqPwedMGWWt8t1yc_KeUejIl3fgmDlJ_VVDk65MRPSubV7GzOROSzni6_e7W6nQC92hSiFLYQY2h56VYGC5Q9w0EbJl4Y8lHx9vxpjiHXbajG_n40zYMTwSsVGalrvFXIbSbmnpHorM0NCjq0ZWau7xmLMehZOolFTJb0QQawW9fqJrYknckw80qiSCqmk7WF5BjxMSAtR-D3A9nwm_QbO91HU__FXtB0jPFzrP_jHdCh8qj9s5VLTmTF6BOLCEm3MHWdGUB2UvA9oYKzowJpp1Jy1xjQ6JhbS4aG4vrj092EtIsvKE1Mgu-Hj8tx00gFZpKvPPCi2K6d299zzS1MzYeRk1irqJZ9usgXbxYUBUUk-IPVrp2qNXFayp-VLC1qaarieEy1Q_q4zLXND9oNdIYcyKSCMr9sNCoV6MhEH1PsNUmrcqkOaHmzUznqty6M4P2D0NjRP7B8SgGazKzIEQBTQb7KYlSKZogzVay5LuGXwIJjOeSTF_j1YwC8Oy4Gpe8JiST6O5QmLo9eQSDwHUshQQC5E6NGKrVqtg4nw6QsDcUiWJMZbk5ynptd10IpTZmy8kaSC-qCwHqVF-H9x-pdu0Do_oBRsqI39JtFqGG818IwmXYKblt7N7-RDF2hblBX-y80CPkm_FYCH6sZoMfBpGUy_eZksZMU79Gnz_t69N4vAs6SP5z1y55bY6BLYeyk_8gjMy4Lh21VnNmhJvQ6fY94QbtjxmNRaRLfNlKpotIUAnFqOzUy-jdtkNB84KyytOZO5L2hveM4TSiRjHkTxsMbCzLCIMazzVSjqKd1Tdaao0RBNB1cIo_Nv6t2FQN5hCBLrF1wstHHosiYml6e8WR9_wiF8h-TDVk5vqM2EDa4qmxikJB1-J_Yc9pO0cYnOPBE_8IAhlxhRoIWVZUSLjQQlxZK1spFvxyw1O3JUqI0kG_t-ZG1jcqZlqnrGRAGDLDz8VkB-ac9Mm1bCy9W2VUTCYT2GX8PPpv4I7VxlhM2efsDCbMCN7GSTjL9njWiRlviwHbKVKum5590w9Gu79Wwe0nrpK2Dx97ouMd72O3jYEqBPk72Mq0pCzNgBBPnHEWHKMobjJcfuuslflsUR7QpHeFX0kB4z5PJiU6TByK6k-Lbk-UQQtNEKielBP-9z060xyTLY1Jco1Bk2xOK1nBAKfgeX16W7TG26DG-IoonMw6pc-FQ_ozdju7dbcVhpweUOKkSwCU8UX7D-Uj-8RMFLBqovXSMz9N3JnVlnlOtSziQo6OlT4sqAHwbMQry5Yeh-gKzmtgV26PelWZWSZhV75xWP1UwzuB0cBRqo4XqPQuAE6oJGvfmBpEjWoQwJB4FYFG1xS7h7k72Or1DGwBn4bNSM2vbTSISy0A8lCf2w88uO1q0VElAN88d2lfA0In1Hsx7ly13Sj1eGXka-u492vzdRMxaSScx0QwIRmRSKeQwX0kfrpvz9yCZTEdue_MHUFX4RJEkmT4YDFUB0YuxK0I4tG1LZ31A4H_2Kp-dem2yTfeSiGYcgikQ","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6IkJPN09VaENVellNemFfTUZ1aHVpeXFiN085bzRQRTk1RVhTdWpYMHlHUUUifSwiYXB2IjoicFpXOFdTWm9YNnNrXzNaRm1ramFtWXBpOGZGUVlwQ3h2RFBlaUJwT3BrMCIsInNraWQiOiJkaWQ6cGVlcjoyLkV6NkxTcHBaYmtMVEpOMm5nRDF2YVhFV2MycTc5OE1DRjNKQzlReWNZWlZwQkVpMlIuVno2TWtoUDhONmp5eng3MmdVQlQyYnVpcVE3VE1WRVFBNENoY3ZLVVBNYmVWN1hjTSM2TFNwcFpia0xUSk4ybmdEMXZhWEVXYzJxNzk4TUNGM0pDOVF5Y1laVnBCRWkyUiIsImFwdSI6IlpHbGtPbkJsWlhJNk1pNUZlalpNVTNCd1dtSnJURlJLVGpKdVowUXhkbUZZUlZkak1uRTNPVGhOUTBZelNrTTVVWGxqV1ZwV2NFSkZhVEpTTGxaNk5rMXJhRkE0VGpacWVYcDROekpuVlVKVU1tSjFhWEZSTjFSTlZrVlJRVFJEYUdOMlMxVlFUV0psVmpkWVkwMGpOa3hUY0hCYVltdE1WRXBPTW01blJERjJZVmhGVjJNeWNUYzVPRTFEUmpOS1F6bFJlV05aV2xad1FrVnBNbEkiLCJ0eXAiOiJhcHBsaWNhdGlvblwvZGlkY29tbS1lbmNyeXB0ZWQranNvbiIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJhbGciOiJFQ0RILTFQVStBMjU2S1cifQ","recipients":[{"encrypted_key":"c7XhowLVuquZg6RHkomh316LxOGKMyFUqKfXTO2gog_kxEqD8OKEBgRcn-_FkJisVHaAF0sCHetodMzyiEZvG0ln0eFQRRtr","header":{"kid":"did:peer:2.Ez6LSpou63sBDB4FGpbVM23bECgZnkMHj6hGmA3PgQByR9fs4.Vz6MkhNpHBCUgBgkCbiM4zMjrbfgGowwEuEchmzf6J5W3av8E.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6ODA4MC8iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19#6LSpou63sBDB4FGpbVM23bECgZnkMHj6hGmA3PgQByR9fs4"}}],"tag":"o4NlnjRNx3pfg6YxIQZQrEZB3XHGoZoAPjWIjs9YkH4","iv":"-rwQrmqrIDj_aKHDP7nSsA"}"""
    """{"ciphertext":"v5eC0fdnxe2LZYJSbEv7xXbxbXZ63Romq3ZI7vz-eWUOHpGXaDoQhQjjgLU8ycdbDgDdLvoRLLoXlIchWWX4PbkV04wnZhOsMFXxwPr79QXPDQa28CXri0SSGz7bh5yhGcm_McVFwxVfDyUlVLr6rx7E8r6lrt2EnGhSRIWDdHq40SLtwWJA6kMstLnNzc3Xu6D39Edfadfh-96So1gJJ5ooKiFv5MZBQz7OP04qHXvucs4VSr-giF-8CStCkZ1yDTPOgvdsp7lD0wyyMMqT3dh-ur1aQfLnnqETqURL5xdXZIX0AKgTiWr3UZGNeGWG7rcEUaMp-nLkhmnFdrGv1lkKiFKQo9W8tblUZwZWrf7LlIleI17L84cLR5zhMy1NFMQbIphrLtVcGMPAY7wZA15UWfYNFWC54XQcKSwrarf-zVl6lil2saIjWd1Soa2IaqFZI0p4IJNDq-zJBHju4TAhHa-6R8HBvLUhJ0D7jOm37Xtr7y63qdgydET4MYNjmoiOXfQS1aden64voNwWXGAT-8UG6LP98Xo-g8kJvrFzTIC_sjL6b6PGF6BSDx0i-JXA6kS7fXTLZ1U0sfyo3_IyxMvqxJCD27rY0a8TJYUxl-n3dv-b-JAQUVQqwXmb8YKdVrz9MkKA-_SJx4l_JiUA6-yyey4aORvtqwtyJDzRlchfiHSykg1UN2GBWha5BuLRyigv98DitfraXiRaP7DwOoxcfpzy6r-liU1SF8j8qw_RNefJ0GQhrbT5oNsDNEmfH9HWcCIsMdsW7bG8esW6k9j2uF9BII6aP_enrnpmYQEPSS72BU9BnD2Dq8pzIEzU34QAHS8hO-IN8wLC7aMSNKWO0GZnMHqEQWGCiK4c_0HcAV9AJoqiHLRat4H34AdMjkUxQ-kcbuBdMATG5LTOmHiildQ7gsA0Hs9f_4DlD5NeVHSlgRCWfCv9LVbraGBLFiTikZ5FHewqOw5EeYka0pr1mCfvA1EhpDB0C5KlsDAonHEhqMqvHrbyCrzsZtUCYItC0H49Gu9LrB95Ln92Y1HmZ7ScRnxyBlGqlX1O-H0AKlkQ7cKQtKYI0fA9j9XGuyOeEkFXzGWfX6nwVUcU84KjgU61k1KDNPkk2BUbtaUmwHqvzIcYROTJwuoPAhp_p7mzvy7ntm3Ls0qEk67PyCddY7Pnhq-b6CPbyHauleDR5h7x1MliY-Q6jxFBlY4Pel0TydOZmMi7DOwnT3MTfwFK9dE7rJ4muwA6kfsThoN5N9SY3ExAdfIgeLEQc9m3Vd3yVPLeGZ5Ehy_z_iuTBU9DL08lnlSFa6m0dLgia23BvHXZt1sznrIqUQ0T6dT5-1Iy-V8lcfcl6r19eAZoT19VwpltMMrwijnjXkObU21RdgsP2hTeRJz-E9vyJjDFVFD-bE5RKlX_E_MmtC__Ogg1K9MPeMZd_f2zu0_3edujuIQuDmWCELFg7otshmsVvyV7FJkigOGCUasrf86BqAXyHaqczwdIEfhW7z4GrFcA2_j4-15m8DQn-SOvZDiDFPD2UFqX3t6fFBWVn7OVYrm85UBVuvogRlX0bJRCZ8QKOAzsutQTfbxG9Dm04L4vpp41rCBosUxSm_PxlPcCmErctbZax_Zj9PCJF80OJNqPb0TOUiYMh1q5jD1boeVZNcHuU1cBf-OhZdbYn0OivX0NTx1X4OhAMBxww99h59BzddpL7k8s7RxrlD5bQkgpJ3qJxfFjXMCLNk5plI5001S3uq1vxJIp5Ptf_-9Lx1C4Bn8QsGL9-y0-cp6BKi8Bdj4-dmuo-C6vsXyUcjHwpjlujRhMzgAZDbZ3xBudd9VZSMfJzJQye8Z2kkQzWFx8586XYi5qSLf7tdyJvc1dF2V7YHRrKVKL_tgZ6LcO368_wZlTCpRyFuJJMDh_YthYtTv1dWBf79Yec2jwxl3VJ02GZ07MIQKQwp9Cfo07-shcY6dywqfnI8aR5LuLOZEgEmr0OU1r6RdWHBI7Ii-w4-1OCOR3yaXggSpq8oXGLfk_E4jGu9G_2hh939YmuKUcAiV9bqb39a-88x4AJRqXvh41m89iakoYOFinfjvcP2EMZfPKhCSq7QOhd0CNNvrzYtT0AJmdQz7VX9zhqSjq72HCSC1jon7zGGp79As5V4JhtIqI12cmNRs66JZxvx6nD1WpaVVffzxklShVfdG403Clhy57LxDIBDDa_uGGwUWF1gGnJFu6JuA6flWg_-Mgc01a7z3XDaxGYNj_YhC32fyh0L6F4fCavt_1LVRYUV38qcI0IOyShpWS362nBd7Z1jbUsZ60HPiTKMseVrsZOeRxW0whJJ5fYhKbuD4MaLS07kE_lscn1M8dIfOgjbF1KsI7NykWMIJhOJ8H2o2d0OKuf9BZieHQF96O15jCJ3V1-LY__-eclDRhjG0a76oP8BR3Sq0lXfQlmGXD7xLUffvIBJDzoCDwW3jquMzDyo8WqOgwCsu0kKL0uvekd0SVzPQClWd4gvQwPGiULR9FW8ZFyShrsqIX3lJFrht1qZF_alhNcuw29JU0BLFQjYTecaZqAETg7YCuH9-Y6dgDYpDFmoMbqXo_uLWZtAybALNEkhJ1AAWTnJHxBcuxVw4P_pPkgWnhlwx1t7xvNKrPSLHFdbPilRlaMg9CA7C9dn5YKxqE3nQXH4sDDJ7snxEkzXRgbQsTGni9BS8WKOHULc-HyTvtObJNja0JYJTJtAiPUmJWp7CQH7qspPAeIr8iM8HRvBe4oiAplyU3R2Scn5DGWc4D1EE7siygofUTDUF3mlROhdzHqJhiLIBnCuCO3EFyIqgFpvN36JtRTrsrmUZ7sse43q9JE1ra-XvQigUAY_JFvd6-l-wQ3uuF5d_c0gA75FtcIHtoq10dmouhpsRb0aC9Ju3qFvDBFT2HK1YaFeEMKaxDawvyPMIhJwv2HOF5N6jGRYDj5msfSvIseiDeIQx3j4Gxas-oYfdAOfPuMmKcHj5QEHVxE0qOjp5RUHqKzv4DNinuDk8lCUYFI4nFcJ62rD_QNGPMvo0X17vA53lvQPl7hHLKHuqobed3t_yWJmNYRHmqmTl_7YLkdcvaHXvozDmw83gSmeKioKAQZGxpBAQYGiwmrdrOH8JZNAdfGSVQvFCNrQSvxvcLR07hfQ500xYZ4gXbdLaUUZvLPsevz3S58DVOY1sHb9406UjTB95zylbbIJDonnRNs0uJAaS238uwJZv-8U383uNeBtKS__b9TM7RGSGR1glHPxmN64CagHxTHcekikmti5dvelfxlfkvXXm8nR_n9TeOeXXth1_13xVrhLGR7SPg1xde6m3NgZeCycew-_D81IxyJ0pg_Yyqx76YTM_5ek1fqk7aTIdS-9wC0xHgLPhuvkC20d2vzlz01S2udfpURqfD2STneswIHYBaMba5ZDmLj-GG4byfXrbKOvryjTgZlQQCtRSD8LPNu7jjx7Sj37IWTuWGji8XsgRNijLecZ42zA1555VQqYhfx5eNJ1Qtw1bqIquXSYKeE12H45-BH3yeNRLJOGZyWXk44eaqrALxRQwTVAQTzU_bxTjMBHkdKCU1kwMG9XTejcKpF5_V_fn9MZr1o1xl1p71Nw6j0aeZMyeNz93b15O7XRn-n1P19DYkGl2doQsnxdRw5uVbBibSJEHJiXXkPk1W1LmKeBLT-XPJM8b7HD4iSBbDpV30BmWxFTcWBA-LpxtntQqMofZFX50tcKD8-vkqbOS0CWq_sRcqmwPofubaCjNoacDrvpLd6defwVmlPAnqTQ7aN9gmG8X6nG8h91_jXZuPQwfj6lhblQb3X78cGBOqzzKWIYy9KWOOJdxNJUqRs7nJdRLRKsm038LHAeKdJaxpk9d4G17UtFPvsCXHoScqm7KZpOpC6OPz3wkoccqKewd7fUuPtctpqBwxHBvom6Wu6w9tM9M-fSrl_TnYsLS0jDRYdjtmrbrCy5pvLFKYQSCZyQAm1d7BWg8wP29GLQyi581xsSv-s60Z1FRi56hnMzIHr0SfSCsFneml-63b0JbD4dswxM_GWmQzhf4CBlhazN-rnqkSxZZCP5mtzRTs12ppAccXs14S2f51UExclUADIWyr7EY4tz3vOXgpOuQbd26uIbuRZCj8GHsyXVy-J1ovKFjiGH9nKxfVQYJvQL7o1V0G9IA0czQ6R9QFHfxB1SI7U9Us3JdLdzrJ7DSJfOlVThfNoUyABdYdERhuhoC315l_D35yEcOpocX_4EvR_HD10LsoErVuWDa4M5XNiduEFlNtlUcev35zS2LEVSJpTnRvCD4tUZWKxNe_hHn5GzDaUYaMbB1bbItmPRmNiSspdDDKDf9ZJ8Nnntdw9cDtd6uagkg7ArSoSjHcjc6MDm7B-HWjMQQMpoHcGLcvUJwC78fvNpFosfSRJSHGVd3NdL1zQImo31FgKQDfXa4044BBm-hiT2_mNVQUr2bl6m9vqkEnbLgVzr22N9_ire4BKnVd9gs5DtguIL2aXXolPDmF14q4Yk4BhUxCVRiD4Tz29po4q3XWJHQ1AyJMmydkm6FSCsCn5ha6yqNrju71otjgYuU5Nt-Z8yYZa98FLhSokfiKNw2zHsnjm-qq1OWgKXUX_tDfbHQXWq-ymjUbT9Hi4sZWeO6l56VaB-iMiph5ScAINq6x0ucO5Rb087iRZZpooG1E0c5vxVtfv2HC2MyGWrWvDRiq2w15cXCpDFDgQC_t_4UhrJ0CRynLAXnX358tk364i07ANVGGlhiQKoEJxg4Q5Yv1dcOuyyTKcwsh-0MsF0qccKT9JrfejqZFu3CFKx2t23NJKdDxBmYG4_K2LVTUzsSGD8fEbaTS16azYkMq9KYquB3hZCXHjQuvu9MXIpCoZCitCS5LT-DwnFH5K1oxvYalQA6XPyfAFFS1zWvDVxOv0RvWxG7l0Y2-jeOCgdKnewYExFLfjC3wxv4hO4lk2L1H6EcuOo4AQYQwxJzHHkEw07r6hC60w2q9WwYL1cYaXcFLvvT5HamushF5wc4ogPzeo-vMB8XbdXYDnIBdevdY7WyArqjFwBPdHnxLXO9ASEXUaRIXDVy9v_fUcmcVDZIn6G3e1uTo_5ECu-xzzer5QFAp1mNASr4KlEnKwYIZWwPQJ6_wNNnuz9eg8j8HiQGdJO2gRZZvHMazyvxKgXTMcoQ9gtzShOEY_8TOJYVCF6Tg1o__gZihxM1hEBzbFoTy3i3G_QRkexeRayqegkPc8JZP7kbzYNQjN8W5ArOwws24fQZaOH54pqD-dkYc5GeFtzMm40v8hWSxOSpUXZnKhsvd3_euMTrrJhHckWUdj4hYOUyB1LD71Enfkov3OTkAYVvGZodBM9oO2t7vVtWeBL9HXx0cFQgVVzMzMPRmBx6Dz-Y9r9Gz505srqDpgF6z_ClJjfH_vIW51wDAQz9VgRfYKvmSWAuF94VxdI3s58M0eaM","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6IjVtYTJNcUFEM295cU11X3d2RHdJVFFJWm43UTRNQkhicXdaVE43OVhwRHMifSwiYXB2IjoicFpXOFdTWm9YNnNrXzNaRm1ramFtWXBpOGZGUVlwQ3h2RFBlaUJwT3BrMCIsImVuYyI6IlhDMjBQIiwiYWxnIjoiRUNESC1FUytBMjU2S1cifQ","recipients":[{"encrypted_key":"QifvalQ7BJpXotPnf7bj76IbWew3eNLtULmwDcJ_W13bHrP3JCpiQg","header":{"kid":"did:peer:2.Ez6LSpou63sBDB4FGpbVM23bECgZnkMHj6hGmA3PgQByR9fs4.Vz6MkhNpHBCUgBgkCbiM4zMjrbfgGowwEuEchmzf6J5W3av8E.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6ODA4MC8iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19#6LSpou63sBDB4FGpbVM23bECgZnkMHj6hGmA3PgQByR9fs4"}}],"tag":"1DK3GmQ41inpuZcpu_6TuQ","iv":"ZJDpJg-j7buDQP3TlemN1fKFHbRwaEgy"}"""
  val message = data.fromJson[EncryptedMessage].getOrElse(???)

  val program = for {
    _ <- Console.printLine("""### Example Main ###""".stripMargin)
    _ <- Console.printLine(message.toJsonPretty)
    msgAfterDecrypt <- anonDecrypt(message).provideSomeLayer(Agent0Mediators.agentLayer)
    // msgAfterDecrypt <- authDecrypt(message).provideSomeLayer(Agent0Mediators.agentLayer)
    _ <- Console.printLine(msgAfterDecrypt.toJsonPretty)
    _ <- Console.printLine("#" * 80)
    msg2 = msgAfterDecrypt
      .asInstanceOf[PlaintextMessageClass]
      .attachments
      .get
      .head
      .data
      .asInstanceOf[AttachmentDataJson]
      .json
      .as[EncryptedMessage]
      .getOrElse(???)
    afterDecryot <- authDecrypt(msg2).provideSomeLayer(Agent1Mediators.agentLayer)
  } yield ()

  val operations: ULayer[Operations] = MyOperations.layer
  val resolvers = ZLayer.succeed(DidPeerResolver)

  Unsafe.unsafe { implicit unsafe => // Run side efect
    Runtime.default.unsafe
      .run(program.provide(operations ++ resolvers))
      .getOrThrowFiberFailure()
  }
}
