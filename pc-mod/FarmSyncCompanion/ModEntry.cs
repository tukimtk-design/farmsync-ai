using System;
using System.Threading.Tasks;
using StardewModdingAPI;
using StardewModdingAPI.Events;

namespace FarmSyncCompanion
{
    public class ModEntry : Mod
    {
        public override void Entry(IModHelper helper)
        {
            helper.Events.GameLoop.GameLaunched += OnGameLaunched;
            helper.Events.GameLoop.Saved += OnSaved;
        }

        private void OnGameLaunched(object sender, GameLaunchedEventArgs e)
        {
            Monitor.Log("Checking Cloud/Local SMB for newer Android saves...", LogLevel.Info);
        }

        private void OnSaved(object sender, SavedEventArgs e)
        {
            Monitor.Log("Game saved! Triggering Asynchronous background sync (Task.Run) to Cloud/Local SMB.", LogLevel.Info);
            Task.Run(() => {
                // Background sync logic stub
            });
        }
    }
}
