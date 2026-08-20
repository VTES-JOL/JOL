import {useEffect, useRef, useState} from 'react';
import {api} from '../../api/client';
import type {UserSummary} from '../../api/types';
import {Card, CardHeader, CardTitle} from '../../components/Card';
import {useSimpleTooltips} from '../../hooks/useSimpleTooltips';

// No WS scope for this one, deliberately — "online" (PlayerService.activeUsers())
// is a 30-minute HTTP-activity-recency roster, not a WebSocket presence concept;
// there's no discrete event when a player "ages out" of that window, so a plain
// interval matches the data's actual precision better than inventing a push
// signal that wouldn't reliably correspond to it anyway.
const REFRESH_INTERVAL_MS = 60_000;
const OFFLINE_THRESHOLD_MINUTES = 60;

const regionNames = new Intl.DisplayNames(['en'], {type: 'region'});
const LAST_ONLINE_FORMAT = new Intl.DateTimeFormat('en-US', {
    timeZone: 'UTC',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZoneName: 'short',
});

function minutesSince(timestamp: string): number {
    return (Date.now() - new Date(timestamp).getTime()) / 60_000;
}

function UserRow({user}: { user: UserSummary }) {
    const isOffline = minutesSince(user.lastOnline) > OFFLINE_THRESHOLD_MINUTES;
    return (
        <div className="online-player-row">
            {user.country && (
                <span
                    data-tippy-content={regionNames.of(user.country)}
                    className={`fi fi-${user.country.toLowerCase()} fis`}
                />
            )}
            <span className="flex-grow-1 text-truncate">{user.name}</span>
            {user.roles.includes('ADMIN') && (
                <i data-tippy-content="Administrator" className="bi bi-star-fill text-warning"/>
            )}
            {user.roles.includes('JUDGE') && (
                <i data-tippy-content="Judge" className="bi bi-person-raised-hand text-success"/>
            )}
            {isOffline && (
                <i
                    data-tippy-content={`Last Online: ${LAST_ONLINE_FORMAT.format(new Date(user.lastOnline))}`}
                    className="bi bi-clock-history text-muted"
                />
            )}
        </div>
    );
}

export function OnlineUsers() {
    const [users, setUsers] = useState<UserSummary[]>([]);
    const listRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const refresh = () => {
            api
                .get<UserSummary[]>('/main/online')
                .then(setUsers)
                .catch((err) => console.error('Failed to load /main/online', err));
        };
        refresh();
        const interval = setInterval(refresh, REFRESH_INTERVAL_MS);
        return () => clearInterval(interval);
    }, []);

    useSimpleTooltips(listRef, [users]);

    return (
        <Card className="overflow-y-auto">
            <CardHeader>
                <CardTitle>Online ({users.length})</CardTitle>
            </CardHeader>
            <div ref={listRef} className="overflow-y-auto">
                <div className="card-body p-1">
                    {users.map((user) => (
                        <UserRow key={user.name} user={user}/>
                    ))}
                </div>
            </div>
        </Card>
    );
}
