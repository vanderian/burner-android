package com.vander.burner.app.ui

import android.content.SharedPreferences
import at.favre.lib.hood.Hood
import at.favre.lib.hood.extended.PopHoodActivity
import at.favre.lib.hood.interfaces.Config
import at.favre.lib.hood.interfaces.Pages
import at.favre.lib.hood.interfaces.actions.SingleSelectListConfigAction
import at.favre.lib.hood.interfaces.values.SpinnerElement
import at.favre.lib.hood.interfaces.values.SpinnerValue
import at.favre.lib.hood.util.PackageInfoAssembler
import at.favre.lib.hood.util.PageUtil
import at.favre.lib.hood.util.defaults.DefaultButtonDefinitions
import at.favre.lib.hood.util.defaults.DefaultProperties
import com.f2prateek.rx.preferences2.Preference
import com.jakewharton.processphoenix.ProcessPhoenix
import com.vander.burner.BuildConfig
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.Injectable
import javax.inject.Inject

class DebugActivity : PopHoodActivity(), Injectable {

  @Inject @field:Xdai lateinit var poaUrl: Preference<String>
  @Inject lateinit var prefs: SharedPreferences

  private val urls = listOf(
      SpinnerElement.Default("", "http://localhost:8545"),
      SpinnerElement.Default("", "https://dai.poa.network")
  )

  override fun getPageData(pages: Pages): Pages {
    val firstPage = pages.addNewPage("General")

    firstPage.add(DefaultProperties.createSectionAppVersionInfoFromBuildConfig(BuildConfig::class.java))

    firstPage.add(Hood.get().createSpinnerEntry(SingleSelectListConfigAction("Xdai URL", object : SpinnerValue<List<SpinnerElement>, SpinnerElement> {
      override fun onChange(value: SpinnerElement) {
        if (poaUrl.get() != value.name) {
          poaUrl.set(value.name)
          prefs.edit().commit()
          ProcessPhoenix.triggerRebirth(this@DebugActivity)
        }
      }

      override fun getAllPossibleValues(): List<SpinnerElement> = urls
      override fun getValue(): SpinnerElement = poaUrl.get().let { SpinnerElement.Default("", it) }
    })))

    firstPage.add(DefaultProperties.createSectionBasicDeviceInfo())
    firstPage.add(DefaultProperties.createDetailedDeviceInfo(this))

    firstPage.add(Hood.get().createHeaderEntry("Misc Action"))
    PageUtil.addAction(firstPage, DefaultButtonDefinitions.getCrashAction())
    PageUtil.addAction(firstPage, DefaultButtonDefinitions.getKillProcessAction(this), DefaultButtonDefinitions.getClearAppDataAction())

    firstPage.add(PackageInfoAssembler(PackageInfoAssembler.Type.USES_FEATURE, PackageInfoAssembler.Type.PERMISSIONS).createSection(this, true))

    firstPage.add(DefaultProperties.createSectionConnectivityStatusInfo(this))

    firstPage.add(DefaultProperties.createInternalProcessDebugInfo(this))
    firstPage.add(DefaultProperties.createSectionTelephonyManger(this))
    return pages
  }

  override fun getConfig(): Config {
    return Config.newBuilder()
        .setShowHighlightContent(false)
        .setLogTag(TAG).build()
  }

  companion object {
    private val TAG = DebugActivity::class.java.name
  }
}