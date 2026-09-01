import { useCallback, useState } from 'react';

// Judge-only "show commands" toggle for the game chat log, remembered per
// viewer. GameChatPanel and HistoryPanel are never mounted at the same time
// (GamePage swaps one for the other), so a plain localStorage-backed hook is
// enough — no cross-component sync needed.
const KEY = 'jol.chat.showCommands';

function read(): boolean {
  try {
    return localStorage.getItem(KEY) === '1';
  } catch {
    return false;
  }
}

export function useShowCommands(): [boolean, () => void] {
  const [on, setOn] = useState(read);
  const toggle = useCallback(() => {
    setOn((prev) => {
      const next = !prev;
      try {
        localStorage.setItem(KEY, next ? '1' : '0');
      } catch {
        // private mode / storage disabled — toggle still works for this session
      }
      return next;
    });
  }, []);
  return [on, toggle];
}
